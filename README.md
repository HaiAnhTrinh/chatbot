# **Scheduling Chatbot with Dialogflow**

UTS Engineering Capstone project

Supervisor: [Dr. Wei Liu](https://www.uts.edu.au/staff/wei.liu)

Scheduling Chatbot enables the users to book a meeting on the Google Calendar and reserve a room available in a company. The chatbot is able to detect conflicts and suggest the closest suitable timeframe for all attendees. It also helps allocate the meeting to the most suitable room provided within an organisation. All communication will take place on Facebook Messenger.

## **Demo**
There will be a youtube link here for the demo

## **Getting Started**
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

#### **Prerequisites**

1. Follow this link to set up a new project in Google Cloud Platform. For this project, just follow the _Create a project_ section. [Google Cloud Setup](https://cloud.google.com/dialogflow/es/docs/quick/setup#project)

2. Enables Calendar API in the [Google Cloud console](https://console.cloud.google.com/) and create the credentials for OAuth 2.0 Client IDs.

3. Follow the steps in the [Create an agent](https://cloud.google.com/dialogflow/es/docs/quick/build-agent#create-an-agent) section to create a new Dialogflow agent.
  
4. Finish the [Setup](https://cloud.google.com/dialogflow/es/docs/integrations/facebook#setup) for Facebook Integration.
  
5. Install [Intellij IDEA Ultimate version](https://www.jetbrains.com/idea/download/#section=windows)

6. Install [ngrok](https://ngrok.com/download) (if you don't own a public server or domain).

#### **Installing**
**Step 1:** Configure the JDK to be 12.0.1 or above, install all dependencies with Maven.

**Step 2:** Download the created credentials for OAuth 2.0 as `credentials.json` inside `src/main/resources/` directory.

**Step 3:** Modify Spring configurations for H2 database in `application.properties`. Change the datasource.url to suit your working directory.

![Image](file:///C:/Users/trinh/Desktop/UTS/YEAR%205/Engineering%20Capstone/application-properties.PNG)

We use H2 embedded database to avoid complex configuration during the development process.

In case you want to use a different database manager for deployment, the queries to create the tables are in _query.sql_ to help you set up your own database.

**Step 4:** In the Dialogflow console, go to settings and import _chatbot.zip_ to your agent.

![Image](file:///C:/Users/trinh/Desktop/UTS/YEAR%205/Engineering%20Capstone/import zip.PNG)

**Step 5:** Run the project in your IDE, you should see the following contents in your console.

![Image](file:///C:/Users/trinh/Desktop/UTS/YEAR%205/Engineering%20Capstone/server started.PNG)

**Step 6:** Use _ngrok_ to expose your running localhost server with the following command `ngrok http 8080`. This will generate a temporary URL linking to localhost:8080.

![Image](file:///C:/Users/trinh/Desktop/UTS/YEAR%205/Engineering%20Capstone/ngrok.PNG)

Copy the generated URL provided by _ngrok_ and place it as the URL for the webhooks in Dialogflow console.

![Image](file:///C:/Users/trinh/Desktop/UTS/YEAR%205/Engineering%20Capstone/fulfillment.PNG)

**Step 7:** If you completed the prerequisites, you should have an app in _Facebook for Developers_. Add a facebook user as a tester for the app.

![Image](file:///C:/Users/trinh/Desktop/UTS/YEAR%205/Engineering%20Capstone/facebook testers.PNG)

**Step 8:** Insert a new user to USER table in the database. The new user data must have all values except for FB_ID.

**Step 9:** Use the facebook tester account to start a conversation with the chatbot. On the first interaction with the server, the users will be asked to register their identity. This will fill up the missing FB_ID field.

![Image](file:///C:/Users/trinh/Desktop/UTS/YEAR%205/Engineering%20Capstone/register.PNG)