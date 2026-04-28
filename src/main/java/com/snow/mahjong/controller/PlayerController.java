package com.snow.mahjong.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

//プレイヤー登録機能（IDと名前）

@Controller
public class PlayerController {

	@Autowired
	private PlayerRepository playerRepository;

	@Value("${app.upload.dir}")
	private String uploadDir;

	@Value("${app.admin.password}")
	private String adminPassword;

	//    一覧取得
	@GetMapping("/players")
	public String list(Model model) {
		model.addAttribute("players", playerRepository.findAll());
		return "players";
	}

	//    登録画面表示
	@GetMapping("/players/new")
	public String createForm(Model model) {
		model.addAttribute("player", new Player());
		return "player_form";
	}

	//    保存処理
	@PostMapping("/players")
	public String create(Player player) {
		playerRepository.save(player);
		return "redirect:/players";
	}

	@GetMapping("/players/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Player player = playerRepository.findById(id).orElse(null);
		model.addAttribute("player", player);
		return "player_edit";
	}

	@PostMapping("/players/{id}/edit")
	public String update(
			@PathVariable Long id,
			@RequestParam String name,
			@RequestParam("iconFile") MultipartFile iconFile) throws IOException {

		Player player = playerRepository.findById(id).orElse(null);

		player.setName(name);

		if (!iconFile.isEmpty()) {

			String uploadDir = System.getProperty("user.dir") + "/uploads/icons/";

			Files.createDirectories(Paths.get(uploadDir));

			String fileName = id + "_" + iconFile.getOriginalFilename();

			Path savePath = Paths.get(uploadDir + fileName);
			iconFile.transferTo(savePath.toFile());

			player.setIconPath("/uploads/icons/" + fileName);
		}

		playerRepository.save(player);

		return "redirect:/players";
	}

	@GetMapping("/players/{id}/delete")
	public String deleteForm(@PathVariable Long id, Model model) {
		Player player = playerRepository.findById(id).orElse(null);
		model.addAttribute("player", player);
		return "player_delete";
	}

	@PostMapping("/players/{id}/delete")
	public String delete(
			@PathVariable Long id,
			@RequestParam String password,
			Model model) {
		Player player = playerRepository.findById(id).orElse(null);

		if (!adminPassword.equals(password)) {
			model.addAttribute("player", player);
			model.addAttribute("error", "パスワードが違います");
			return "player_delete";
		}

		playerRepository.deleteById(id);

		return "redirect:/players";
	}

}