# 实体类开发指南

## 使用 Lombok 简化实体类

本项目使用 **Lombok** 的 `@Data` 注解来自动生成 getter、setter、toString、equals 和 hashCode 方法，无需手动编写。

## 当前实体类

### Document 实体

位置：`backend/src/main/java/com/markglow/entity/Document.java`

```java
package com.markglow.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String originalContent;

    @Column(columnDefinition = "TEXT")
    private String beautifiedContent;

    @Column
    private String theme;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

## 如何创建新的实体类

### 步骤 1：创建实体类文件

在 `backend/src/main/java/com/markglow/entity/` 目录下创建新的 Java 类，例如 `User.java`：

```java
package com.markglow.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 步骤 2：创建对应的 Repository

在 `backend/src/main/java/com/markglow/repository/` 目录下创建 Repository 接口：

```java
package com.markglow.repository;

import com.markglow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
```

### 步骤 3：创建对应的 Service（可选）

在 `backend/src/main/java/com/markglow/service/` 目录下创建 Service 类：

```java
package com.markglow.service;

import com.markglow.entity.User;
import com.markglow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

**注意**：Service 类也可以使用 Lombok 的 `@RequiredArgsConstructor` 注解来自动生成构造函数，替代 `@Autowired`。

## Lombok 常用注解说明

| 注解 | 功能 | 生成内容 |
|------|------|----------|
| `@Data` | 最常用 | 生成 getter、setter、toString、equals、hashCode |
| `@Getter` | 仅生成 getter | 所有字段的 getter 方法 |
| `@Setter` | 仅生成 setter | 所有字段的 setter 方法 |
| `@ToString` | 生成 toString | toString() 方法 |
| `@EqualsAndHashCode` | 生成 equals 和 hashCode | equals() 和 hashCode() 方法 |
| `@NoArgsConstructor` | 无参构造函数 | 无参构造函数 |
| `@AllArgsConstructor` | 全参构造函数 | 包含所有字段的构造函数 |
| `@RequiredArgsConstructor` | 必需字段构造函数 | 包含 final 字段和 @NonNull 字段的构造函数 |
| `@Builder` | 建造者模式 | 生成建造者模式的代码 |

## 注意事项

1. **IDE 配置**：确保你的 IDE（IntelliJ IDEA 或 Eclipse）安装了 Lombok 插件
   - IntelliJ IDEA: Settings → Plugins → 搜索 "Lombok" → 安装
   - Eclipse: 下载 lombok.jar，运行安装程序

2. **@Data 包含的内容**：
   - 所有字段的 getter 方法
   - 所有非 final 字段的 setter 方法
   - toString() 方法
   - equals() 和 hashCode() 方法
   - 无参构造函数（如果没有 final 字段）

3. **保留自定义方法**：
   - `@PrePersist` 和 `@PreUpdate` 方法会被保留
   - 其他自定义方法也会被保留

4. **字段访问**：
   - 可以直接使用 `document.getTitle()` 和 `document.setTitle()`
   - Lombok 在编译时自动生成这些方法

5. **排除字段**：
   - 如果某个字段不需要在 toString/equals/hashCode 中包含，使用 `@ToString.Exclude` 或 `@EqualsAndHashCode.Exclude`

## 示例：排除字段

```java
@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @ToString.Exclude  // 不在 toString 中包含密码
    @EqualsAndHashCode.Exclude  // 不在 equals/hashCode 中包含密码
    @Column(nullable = false)
    private String password;
}
```

## 自动表结构创建

当应用启动时，Hibernate 会根据实体类自动创建或更新数据库表结构（`ddl-auto: update`）。

启动应用后，检查日志可以看到表创建/更新的 SQL 语句。

