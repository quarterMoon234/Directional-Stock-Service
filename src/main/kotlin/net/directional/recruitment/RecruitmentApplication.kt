package net.directional.recruitment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class RecruitmentApplication

fun main(args: Array<String>) {
    runApplication<RecruitmentApplication>(*args)
}
