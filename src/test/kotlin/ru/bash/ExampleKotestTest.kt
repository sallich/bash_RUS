package ru.bash

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ExampleKotestTest : StringSpec({

    "1 + 1 should be 2" {
        (1 + 1) shouldBe 2
    }
})
