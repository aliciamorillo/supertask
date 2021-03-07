package com.example.supertask.models

import java.util.*

class Task {
    var idTarea: String? = null
    var name: String? = null
    var completed: Boolean = false
    var date: Date = Date()
    var isPriority: Boolean = false
    var category: String? = null
    var image: String? = null
}