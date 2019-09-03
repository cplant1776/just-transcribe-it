# JustTranscribeIt Web App

A web app for users to upload and transcribe audio files into text. Built using the Spring framework and utilizing AWS S3, EC2, RDS, EB, and Transcribe.

## Link to App

[Just show me the app!](http://jti-env-1.us-east-1.elasticbeanstalk.com/) Note: a couple UI elements have not yet been added, such as seeing a transcription job's status (pending, failed, etc.).

## Motivation

I started this project to further expand on what I have learned about web app development and database management. It has 3 primary goals:

1. Utilize AWS resources in a Spring app and gain familiarity with their services
2. Obtain initial exposure to deployment with Docker
3. Gain experience with more complex MySQL functions

## Basic Feature Overview

* User accounts and authentication
* Transcription and storage of user-uploaded audio files
* Authenticated, expiring links to user audio files
* Monthly usage limits

## Some Screenshots

![Home Page](media/screenshots/home_page.png?raw=true "Home Page")
![Account Page](media/screenshots/account_page.png?raw=true "Account Page")

## Current Roadmap (To-Do)

1. Add user authentication to DELETE endpoint
1. Make UI more user-friendly (error pages, more failure message, transcription status)
1. Allow download of .txt file for completed transcriptions

## Current Reflections

For the most part, the project has gone as expected. As I already had some CRUD app experience, most of the difficulty came from getting the different services (S3, RDS, etc.) connected correctly. I had no experience with them prior to this, and it has given me a much better understanding of why AWS and other cloud services can be so valuable.

Matching my previous experience, the greatest difficulty came when transitioning from a local build to a remote server. I kept getting 502 Bad Gateway errors when deploying my raw Spring app to Elastic Beanstock. This ultimately motivated me to deploy the app in a Docker container, which seemed to deploy without issue. I now understand why Docker has been growing in popularity these last few years.

