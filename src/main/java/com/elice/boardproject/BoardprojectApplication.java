package com.elice.boardproject;

import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.elice.boardproject.admin.repository.PermissionRepository;
import com.elice.boardproject.board.repository.BoardRepository;
import com.elice.boardproject.comment.repository.CommentRepository;
import com.elice.boardproject.post.repository.PostRepository;
import com.elice.boardproject.playlist.repository.PlaylistRepository;
import com.elice.boardproject.playlist.repository.PlaylistSongRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BoardprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoardprojectApplication.class, args);
	}

//	@Bean
//	@Profile("local")
	public DataInit stubDataInit(UserRepository userRepository, BoardRepository boardRepository, PostRepository postRepository, CommentRepository commentRepository, AdminRoleRepository adminRoleRepository, PermissionRepository permissionRepository, PlaylistRepository playlistRepository, PlaylistSongRepository playlistSongRepository) {
		return new DataInit(userRepository, boardRepository, postRepository, commentRepository, adminRoleRepository, permissionRepository, playlistRepository, playlistSongRepository);
	}

}
