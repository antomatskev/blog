package com.example.blog

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.server.ResponseStatusException

@Controller
class HtmlController(
    private val repository: ArticleRepository,
    private val properties: BlogProperties,
    private val users: UserRepository
) {

    @GetMapping("/")
    fun blog(model: Model): String {
        model["title"] = properties.title
//        model["banner"] = properties.banner
        model["articles"] = repository.findAllByOrderByAddedAtDesc().map { it.render() }
        return "blog"
    }

    @GetMapping("/article/{slug}")
    fun article(@PathVariable slug: String, model: Model): String {
        val article = repository
            .findBySlug(slug)
            ?.render()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "This article does not exist")
        model["title"] = article.title
        model["article"] = article
        return "article"
    }

    @GetMapping("/article/add")
    fun compose(model: Model): String {
        val article = Article()
        model.addAttribute("add", true)
        model.addAttribute("article", article)
        model["title"] = properties.title
        return "compose"
    }

    @PostMapping("/article/add")
    fun saveNewArticle(@ModelAttribute("article") article: Article, model: Model): String {
        article.author = users.findByLogin("antmat")!!
        article.headline = article.content.split(" ").subList(0, 10).joinToString(" ")
        model.addAttribute("article", article)
        repository.save(article)
        model["title"] = properties.title
        return "redirect:/"
    }

    fun Article.render() = RenderedArticle(
        slug,
        title,
        headline,
        content,
        author,
        addedAt.format()
    )

    data class RenderedArticle(
        val slug: String,
        val title: String,
        val headline: String,
        val content: String,
        val author: User,
        val addedAt: String
    )

}