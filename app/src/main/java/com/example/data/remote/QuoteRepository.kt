package com.example.data.remote

import com.example.data.model.MotivationalQuote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class QuoteRepository(private val apiService: QuotesApiService = QuotesApiService.create()) {

    private val developerQuotes = listOf(
        MotivationalQuote("Talk is cheap. Show me the code.", "Linus Torvalds"),
        MotivationalQuote("Programs must be written for people to read, and only incidentally for machines to execute.", "Harold Abelson"),
        MotivationalQuote("Any fool can write code that a computer can understand. Good programmers write code that humans can understand.", "Martin Fowler"),
        MotivationalQuote("First, solve the problem. Then, write the code.", "John Johnson"),
        MotivationalQuote("Experience is the name everyone gives to their mistakes.", "Oscar Wilde"),
        MotivationalQuote("Knowledge is power, but consistency is progress.", "CodeStreak Wisdom"),
        MotivationalQuote("Code is like humor. When you have to explain it, it’s bad.", "Cory House"),
        MotivationalQuote("Simplicity is prerequisite for reliability.", "Edsger W. Dijkstra"),
        MotivationalQuote("Make it work, make it right, make it fast.", "Kent Beck"),
        MotivationalQuote("The only way to learn a new programming language is by writing programs in it.", "Dennis Ritchie"),
        MotivationalQuote("Before software can be reusable it first has to be usable.", "Ralph Johnson"),
        MotivationalQuote("It’s not a bug – it’s an undocumented feature.", "Anonymous Dev"),
        MotivationalQuote("One bad day of coding is better than no day of coding.", "Streak Mindset"),
        MotivationalQuote("Small daily commits compound into extraordinary engineering skills.", "Developer Habit"),
        MotivationalQuote("Clean code always looks like it was written by someone who cares.", "Robert C. Martin"),
        MotivationalQuote("Optimism is an occupational hazard of programming: feedback is the treatment.", "Kent Beck"),
        MotivationalQuote("The most important single aspect of software development is to be clear about what you are trying to build.", "Bjarne Stroustrup"),
        MotivationalQuote("Deleted code is debugged code.", "Jeff Sickel"),
        MotivationalQuote("Don't comment bad code ‐ rewrite it.", "Brian W. Kernighan"),
        MotivationalQuote("The future depends on what you build today.", "Mahatma Gandhi"),
        MotivationalQuote("Discipline is choosing between what you want now and what you want most.", "Abraham Lincoln"),
        MotivationalQuote("You don't have to be great to start, but you have to start to be great.", "Zig Ziglar"),
        MotivationalQuote("Great software is built brick by brick, one practice session at a time.", "CodeStreak Philosophy")
    )

    suspend fun getMotivationalQuote(): MotivationalQuote = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRandomQuote()
            if (response.isNotEmpty() && response[0].quoteText.isNotBlank()) {
                val item = response[0]
                MotivationalQuote(
                    text = item.quoteText.trim(),
                    author = item.author.ifBlank { "Wise Mind" }
                )
            } else {
                getRandomFallbackQuote()
            }
        } catch (e: Exception) {
            getRandomFallbackQuote()
        }
    }

    fun getRandomFallbackQuote(): MotivationalQuote {
        val index = Random.nextInt(developerQuotes.size)
        return developerQuotes[index]
    }
}
