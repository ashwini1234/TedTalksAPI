TedTalks API


Mission:
To develop a backend system that manages TedTalks data and provides insights into speaker influence.



Tech Stack
Java 17: Core programming language for implementation.
Spring Boot 3.4.2: Framework for building the web application, REST APIs,
H2 Database: In-memory database for storing Ted Talksdata.
JUnit & Mockito: Testing framework to ensure the correctness of the implementation.
Apache Commons CSV (CSV Processing)

Steps 
Clone the repository:
git clone https://github.com/ashwini1234/xx 
Run Mvn clean install
Run TedtalksApplication
Access the APIs via Postman or a web browser:
H2 Console: http://localhost:8081/h2-console
Query REST API: http://localhost:8081/tedtalks/import



High-Level System Architecture
+-------------+        +------------------+        +--------------------+        +--------------------------+
| CSV File    | --->   | CSV Import API   | --->   | Database (H2) | --->  | Influence API |
+-------------+        +------------------+        +--------------------+        +--------------------------+
                            	  |                             	 |
                              	|                              	 |
                              	v                               	v
                       Data Validation              Influence Score 
    Calculation

API Request Flow
User uploads a CSV file 
CSV is validated, if amy errors present in file, all errors are presented to user in one go.
Data from CSV file stored in the database table ted_talks.
Influence Score is computed dynamically 
User fetches TED Talks with paginated results 
Most Influential Speaker / Talk is identified per year

Approach for Determining Speaker Influence
To determine the most influential TED Talk speaker, we consider multiple factors that reflect a talk’s impact and audience engagement.

1️⃣ Key Influence Metrics
Each speaker's influence is assessed based on their TED Talks using the following factors:
Total Views → More views indicate wider reach.
Total Likes → Higher likes suggest strong audience appreciation.
Engagement Rate → Engagement Rate = (Likes / Views) * 100 (percentage of viewers who liked the talk).
Growth Rate → Growth Rate = (Likes + Views) / DaysSincePublished (measures how quick a talk gains popularity).
Number of Talks Given → More talks indicate consistency in influence.

2️⃣ Influence Score Calculation
To rank speakers, we compute a weighted score:
InfluenceScore=(0.4×TotalViews)+(0.4×TotalLikes)+(0.1×EngagementRate)+(0.1×GrowthRate)

Where:
Engagement Rate normalizes popularity across varying audience sizes.
Growth Rate measures how fast influence grows over time.
Weighted factors prioritize meaningful interactions rather than just raw numbers.
