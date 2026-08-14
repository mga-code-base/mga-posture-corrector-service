# 🧘 mga-posture-corrector-service

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot)](https://spring.io/)
[![Java Version](https://img.shields.io/badge/Java-17-007396?logo=openjdk)](https://openjdk.org/)
[![Build Tool](https://img.shields.io/badge/Build-Apache%20Maven%203.9.x-C71A36?logo=apachemaven)](https://maven.apache.org/)
[![AI Engine](https://img.shields.io/badge/AI%20Engine-Posture%20Analysis-FF6F00?logo=tensorflow)](https://tensorflow.org)
[![Hosting](https://img.shields.io/badge/Platform-Railway%20Cloud-purple?logo=railway)](https://railway.app/)

## 📌 Service Overview

`mga-posture-corrector-service` is a compute-intensive AI computer vision microservice responsible for analyzing member workout pose frames and returning real-time posture correction feedback.

Currently Supports - Sqauts

Future Releases - Deadlifts, Pull Ups, Bench Press.

Due to heavy GPU/CPU processing requirements, this microservice is offloaded to run externally on Railway Cloud, while the remaining 10 microservices run on Vivian's localhost Kubernetes cluster.

### 🏛️ Architectural Features
* Computer Vision Inference Engine: Processes incoming workout camera frames to perform keypoint estimation and skeleton pose evaluation.
* Cloud Offloading: Hosted on Railway Cloud to isolate high CPU/GPU compute loads from the primary Kubernetes cluster.
* Standardized Data Contract: Imports shared domain contracts and models directly from `mga-all-models-jars`.

---

## 🛠️ Technical Specifications
* Port: 8087 (Hosted on Railway Cloud)
* Language/Build: Java 17 | Apache Maven 3.9.x
* Runtime Container: Standard JVM Container
* Deployment Platform: Railway Cloud Infrastructure
* Shared Contract: mga-all-models-jars

---

## ⚙️ Build & Deployment Instructions
# Standard JVM Build & Run
mvn clean package
java -jar target/mga-posture-corrector-service.jar

# Spring Boot Run
mvn spring-boot:run

```
