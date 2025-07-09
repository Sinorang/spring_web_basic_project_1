package com.elice.boardproject;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.acc.service.UserService;
import com.elice.boardproject.admin.entity.AdminRole;
import com.elice.boardproject.admin.entity.Permission;
import com.elice.boardproject.admin.repository.AdminRoleRepository;
import com.elice.boardproject.admin.repository.PermissionRepository;
import com.elice.boardproject.board.repository.BoardRepository;
import com.elice.boardproject.comment.repository.CommentRepository;
import com.elice.boardproject.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Component
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AdminRoleRepository adminRoleRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    private UserService userService;

    public DataInit(UserRepository userRepository, BoardRepository boardRepository, PostRepository postRepository, CommentRepository commentRepository, AdminRoleRepository adminRoleRepository, PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.adminRoleRepository = adminRoleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 게시판 관련 권한 초기화
        initializeBoardPermissions();
        
        // 초기 데이터 삽입 - 비밀번호 "1234"를 해싱하여 저장
        UserDTO userDTO = new UserDTO();
        userDTO.setId("testid");
        userDTO.setPwd("qweasdzxc123!");  // 원본 비밀번호
        userDTO.setName("testuser");
        userDTO.setNickname("testNick");
        userDTO.setEmail("test@example.com");
        userService.join(userDTO);  // UserService에서 자동으로 해싱됨

        // 추가 테스트 사용자
        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId("admin");
        userDTO2.setPwd("1234");  // 원본 비밀번호
        userDTO2.setName("관리자");
        userDTO2.setNickname("admin");
        userDTO2.setEmail("admin@example.com");
        userService.join(userDTO2);  // UserService에서 자동으로 해싱됨

        // 실제 User 엔티티 조회 (id로)
        User user1 = userRepository.findById("testid");
        User user2 = userRepository.findById("admin");

        // SUPER_ADMIN 권한 및 ADMIN 역할 생성/부여
        // 1. SUPER_ADMIN Permission 생성
        Permission superAdminPermission = permissionRepository.findByPermissionName("SUPER_ADMIN").orElse(null);
        if (superAdminPermission == null) {
            superAdminPermission = new Permission();
            superAdminPermission.setPermissionName("SUPER_ADMIN");
            superAdminPermission.setDescription("슈퍼 관리자 권한");
            permissionRepository.save(superAdminPermission);
        }

        // 2. ADMIN 역할 생성 및 SUPER_ADMIN 권한 포함
        AdminRole adminRole = adminRoleRepository.findByRoleName("ADMIN").orElse(null);
        if (adminRole == null) {
            adminRole = AdminRole.builder()
                    .roleName("ADMIN")
                    .description("관리자 역할")
                    .permissions(new java.util.HashSet<>()) // 명시적으로 초기화
                    .build();
        }
        if (adminRole.getPermissions() == null) {
            adminRole.setPermissions(new java.util.HashSet<>());
        }
        if (!adminRole.getPermissions().contains(superAdminPermission)) {
            adminRole.getPermissions().add(superAdminPermission);
        }
        adminRoleRepository.save(adminRole);

        // 3. admin 사용자에게 ADMIN 역할 부여 및 isAdmin=true
        if (user2 != null) {
            user2.setAdmin(true);
            user2.setAdminRole(adminRole);
            user2.setAdminGrantedAt(java.time.LocalDateTime.now());
            user2.setAdminGrantedBy("system");
            userRepository.save(user2);
        }

        // Board 생성
        var board1 = com.elice.boardproject.board.entity.Board.builder()
                .user(user1)
                .name("자유게시판")
                .description("자유롭게 글을 쓸 수 있는 게시판입니다.")
                .build();
        var board2 = com.elice.boardproject.board.entity.Board.builder()
                .user(user2)
                .name("공지사항")
                .description("공지사항을 올리는 게시판입니다.")
                .build();
        boardRepository.save(board1);
        boardRepository.save(board2);

        // Post 생성
        var post1 = com.elice.boardproject.post.entity.Post.builder()
                .board(board1)
                .user(user1)
                .title("첫 번째 자유글")
                .content("안녕하세요! 자유게시판 첫 글입니다.")
                .build();
        var post2 = com.elice.boardproject.post.entity.Post.builder()
                .board(board2)
                .user(user2)
                .title("공지사항 안내")
                .content("공지사항 게시판입니다.")
                .build();
        postRepository.save(post1);
        postRepository.save(post2);

        // Comment 생성
        var comment1 = new com.elice.boardproject.comment.entity.Comment(post1, user2, "관리자가 남긴 댓글입니다.");
        var comment2 = new com.elice.boardproject.comment.entity.Comment(post2, user1, "유저가 남긴 댓글입니다.");
        commentRepository.save(comment1);
        commentRepository.save(comment2);
    }

    /**
     * 게시판 관련 권한들을 초기화합니다.
     */
    private void initializeBoardPermissions() {
        // 게시판 관련 권한들 생성
        String[] boardPermissions = {
            "BOARD_READ", "BOARD_CREATE", "BOARD_UPDATE", "BOARD_DELETE"
        };

        for (String permissionName : boardPermissions) {
            if (permissionRepository.findByPermissionName(permissionName).isEmpty()) {
                Permission permission = new Permission();
                permission.setPermissionName(permissionName);
                permission.setDescription(getPermissionDescription(permissionName));
                permissionRepository.save(permission);
            }
        }

        // ADMIN 역할에 모든 게시판 권한 부여
        AdminRole adminRole = adminRoleRepository.findByRoleName("ADMIN").orElse(null);
        if (adminRole != null) {
            for (String permissionName : boardPermissions) {
                Permission permission = permissionRepository.findByPermissionName(permissionName).orElse(null);
                if (permission != null && !adminRole.getPermissions().contains(permission)) {
                    adminRole.getPermissions().add(permission);
                }
            }
            adminRoleRepository.save(adminRole);
        }
    }

    /**
     * 권한명에 따른 설명을 반환합니다.
     */
    private String getPermissionDescription(String permissionName) {
        return switch (permissionName) {
            case "BOARD_READ" -> "게시판 조회 권한";
            case "BOARD_CREATE" -> "게시판 생성 권한";
            case "BOARD_UPDATE" -> "게시판 수정 권한";
            case "BOARD_DELETE" -> "게시판 삭제 권한";
            default -> "게시판 관련 권한";
        };
    }
}
