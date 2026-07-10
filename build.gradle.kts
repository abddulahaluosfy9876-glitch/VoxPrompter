// ملف جذر المشروع الرئيسي - تم تبسيطه لتأمين العبور والنجاح
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
