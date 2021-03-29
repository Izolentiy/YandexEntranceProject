package com.example.entranceproject.di

import javax.inject.Qualifier

@Retention(AnnotationRetention.RUNTIME) // I don't know how it works under hood
@Qualifier
annotation class ApplicationScope