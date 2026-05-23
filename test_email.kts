val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

val invalidEmails = listOf(
    "notanemail",
    "@example.com",
    "user@",
    "user @example.com",
    "user@example",
    "",
    "user@.com",
    "user..name@example.com"
)

invalidEmails.forEach { email ->
    val matches = email.matches(emailRegex)
    println("$email -> $matches")
}
