package com.ai.egret.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GovDataNetwork

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WeatherNetwork

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackendNetwork