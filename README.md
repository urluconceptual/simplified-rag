# Simplified RAG — Oracle AI Vector Search

This project was implemented for the Special Topics in Databases and Software Technologies course. It consists in a simplified RAG(Retrieval Augmented Generation)
application that allows users to upload documents and ask questions answered exclusively from the selected corpus of documents. Text embeddings
are generated natively inside Oracle Database 23ai using an ONNX model. Answer generation uses Gemini on the "main" branch, but there also is a Groq 
version on the "groq" branch. The application runs locally, the database is hosted on Oracle Cloud (Autonomous Database), the communication with Gemini is done 
through Spring AI, while the communication with groq consists in API calls.

---

## Table of Contents

1. [Architecture](#architecture)

---

## Architecture

![img.png](img.png)