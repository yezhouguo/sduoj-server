/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("org.springframework.boot") version Versions.springBoot
    id("cn.edu.sdu.qd.oj.java-conventions")
}

dependencies {
    /* 1-st party dependency */
    implementation(project(":sduoj-common:sduoj-common-web"))
    implementation(project(":sduoj-auth:sduoj-auth-interface"))

    /* 2-nd party dependency */

    /* 3-rd party dependency */
}

group = "cn.edu.sdu.qd.oj.auth"
description = "sduoj-auth-service"

