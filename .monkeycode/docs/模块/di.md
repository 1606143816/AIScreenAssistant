# di

Hilt 依赖注入模块，提供整个应用的单例依赖。

## 结构

```
di/
├── DatabaseModule.kt          # Room 数据库和 DAO
├── NetworkModule.kt           # OkHttp 客户端
├── RepositoryModule.kt        # Repository 绑定
└── UseCaseModule.kt           # UseCase 绑定
```

## 关键文件

| 文件 | 目的 |
|------|------|
| `DatabaseModule.kt` | 提供 AppDatabase 单例、各 DAO 实例 |
| `NetworkModule.kt` | 提供 OkHttpClient 单例、LLMApiService |
| `RepositoryModule.kt` | 绑定 Repository 接口到实现 |
| `UseCaseModule.kt` | 提供各 UseCase 的实例 |

## 添加新依赖

在对应 Module 文件中添加 `@Provides` 或 `@Binds` 方法：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideSomeRepository(db: AppDatabase): SomeRepository {
        return SomeRepositoryImpl(db.someDao())
    }
}
```
