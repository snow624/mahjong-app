package com.snow.mahjong.controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.snow.mahjong.entity.Player;
import com.snow.mahjong.repository.PlayerRepository;

/*
 * 選手管理Controller
 *
 * 役割:
 * - 選手一覧画面を表示する
 * - 選手登録画面を表示する
 * - 選手を新規登録する
 * - 選手編集画面を表示する
 * - 選手名を変更する
 * - 選手アイコン画像をSupabase Storageにアップロードする
 * - 選手削除画面を表示する
 * - 管理者パスワード確認後、選手を削除する
 */
@Controller
public class PlayerController {

	@Autowired
	private PlayerRepository playerRepository;

	/*
	 * 選手削除用の管理者パスワード
	 *
	 * application.properties の
	 * app.admin.password
	 * を読み込む
	 */
	@Value("${app.admin.password}")
	private String adminPassword;

	/*
	 * SupabaseのプロジェクトURL
	 *
	 * 例:
	 * https://xxxxx.supabase.co
	 */
	@Value("${supabase.url}")
	private String supabaseUrl;

	/*
	 * Supabase Storageへアップロードするための秘密キー
	 *
	 * 注意:
	 * service_role_key はGitHubに直接書かない
	 * RenderのEnvironmentに設定する
	 */
	@Value("${supabase.service-role-key}")
	private String supabaseServiceRoleKey;

	/*
	 * 選手一覧画面
	 * URL: /players
	 *
	 * 役割:
	 * - 登録済みの選手を一覧表示する
	 * - 選手名・アイコン画像を表示する
	 * - 編集画面へのリンクを表示する
	 */
	@GetMapping("/players")
	public String list(Model model) {
		model.addAttribute("players", playerRepository.findAll());
		return "players";
	}

	/*
	 * 選手登録画面表示
	 * URL: /players/new
	 *
	 * 役割:
	 * - 新しく選手を登録するためのフォーム画面を表示する
	 */
	@GetMapping("/players/new")
	public String createForm(HttpSession session, Model model) {

		if (!Boolean.TRUE.equals(session.getAttribute("adminLogin"))) {
			return "redirect:/matches/admin/login";
		}

		model.addAttribute("player", new Player());
		return "player_form";
	}

	/*
	 * 選手新規登録処理
	 * URL: /players
	 *
	 * 役割:
	 * - 登録画面で入力された選手名をDBに保存する
	 *
	 * 補足:
	 * - 現時点では新規登録時に画像は登録しない
	 * - 画像は編集画面から登録する
	 */
	@PostMapping("/players")
	public String create(Player player, HttpSession session) {

		if (!Boolean.TRUE.equals(session.getAttribute("adminLogin"))) {
			return "redirect:/matches/admin/login";
		}

		playerRepository.save(player);
		return "redirect:/players";
	}

	/*
	 * 選手編集画面表示
	 * URL: /players/{id}/edit
	 *
	 * 役割:
	 * - 指定されたIDの選手情報を取得する
	 * - 選手名と現在のアイコン画像を編集画面に表示する
	 */
	@GetMapping("/players/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Player player = playerRepository.findById(id).orElse(null);

		if (player == null) {
			return "redirect:/players";
		}

		model.addAttribute("player", player);
		return "player_edit";
	}

	/*
	 * 選手編集保存処理
	 * URL: /players/{id}/edit
	 *
	 * 役割:
	 * - 選手名を更新する
	 * - アイコン画像が選択されていればSupabase Storageへアップロードする
	 * - アップロードした画像の公開URLをDBのiconPathに保存する
	 *
	 * 画像保存の流れ:
	 * 1. 編集画面から画像ファイルを受け取る
	 * 2. Supabase Storage の icons バケットにアップロードする
	 * 3. 公開URLを作成する
	 * 4. player.iconPath にURLを保存する
	 */
	@PostMapping("/players/{id}/edit")
	public String update(
			@PathVariable Long id,
			@RequestParam String name,
			@RequestParam("iconFile") MultipartFile iconFile) throws IOException, InterruptedException {

		Player player = playerRepository.findById(id).orElse(null);

		if (player == null) {
			return "redirect:/players";
		}

		// 選手名を更新
		player.setName(name);

		// 画像ファイルが選択されている場合だけアップロードする
		if (!iconFile.isEmpty()) {
			String originalFileName = iconFile.getOriginalFilename();

			/*
			 * ファイル名を一意にする
			 *
			 * 理由:
			 * - 同じファイル名でアップロードした時の上書き・衝突を避けるため
			 * - player_選手ID_現在時刻_元ファイル名 の形式にしている
			 */
			String fileName = "player_" + id + "_" + System.currentTimeMillis() + "_" + originalFileName;

			/*
			 * Supabase Storageのアップロード先URL
			 *
			 * icons はSupabase Storageで作成したバケット名
			 */
			String uploadUrl = supabaseUrl
					+ "/storage/v1/object/icons/"
					+ fileName;

			/*
			 * Supabase Storageへ画像をアップロードするHTTPリクエスト
			 *
			 * Authorization:
			 * - service_role_key を使って認証する
			 *
			 * Content-Type:
			 * - 画像ファイルの種類をセットする
			 */
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(uploadUrl))
					.header("Authorization", "Bearer " + supabaseServiceRoleKey)
					.header("apikey", supabaseServiceRoleKey)
					.header("Content-Type", iconFile.getContentType())
					.PUT(HttpRequest.BodyPublishers.ofByteArray(iconFile.getBytes()))
					.build();

			// HTTPリクエストを送信して、Supabaseからのレスポンスを受け取る
			HttpResponse<String> response = HttpClient.newHttpClient()
					.send(request, HttpResponse.BodyHandlers.ofString());

			// 400番台・500番台のエラーが返ってきた場合は例外にする
			if (response.statusCode() >= 400) {
				throw new IOException("Supabase upload failed: " + response.body());
			}

			/*
			 * 公開URLを作成する
			 *
			 * このURLをDBに保存しておけば、
			 * HTMLのimgタグで画像を表示できる
			 */
			String publicUrl = supabaseUrl
					+ "/storage/v1/object/public/icons/"
					+ fileName;

			player.setIconPath(publicUrl);
		}

		// 選手名・画像URLをDBに保存
		playerRepository.save(player);

		return "redirect:/players";
	}

	/*
	 * 選手削除確認画面表示
	 * URL: /players/{id}/delete
	 *
	 * 役割:
	 * - 削除対象の選手情報を表示する
	 * - 削除前に管理者パスワードを入力させる
	 */
	@GetMapping("/players/{id}/delete")
	public String deleteForm(@PathVariable Long id, Model model) {
		Player player = playerRepository.findById(id).orElse(null);

		if (player == null) {
			return "redirect:/players";
		}

		model.addAttribute("player", player);
		return "player_delete";
	}

	/*
	 * 選手削除処理
	 * URL: /players/{id}/delete
	 *
	 * 役割:
	 * - 入力された管理者パスワードを確認する
	 * - 正しければ選手をDBから削除する
	 * - 間違っていれば削除せず、エラーメッセージを表示する
	 *
	 * 注意:
	 * - DBの選手情報は削除する
	 * - Supabase Storage上の画像ファイルは今のところ削除していない
	 */
	@PostMapping("/players/{id}/delete")
	public String delete(
			@PathVariable Long id,
			@RequestParam String password,
			Model model) {

		Player player = playerRepository.findById(id).orElse(null);

		if (player == null) {
			return "redirect:/players";
		}

		if (!adminPassword.equals(password)) {
			model.addAttribute("player", player);
			model.addAttribute("error", "パスワードが違います");
			return "player_delete";
		}

		playerRepository.deleteById(id);

		return "redirect:/players";
	}
}