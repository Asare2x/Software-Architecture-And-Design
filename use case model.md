# Use Case Model

**Introduction**

A use case model shows how a user interacts with the system to achieve a goal. It helps explain what the system should do from the user’s point of view.
For this project, the main user of the system is the User. The user interacts with the Share Price Comparison Web Application to search for share price data, view charts, compare companies, and access saved data.

Main Actor

Actor: 

User

The User is anyone using the application to retrieve and view share price information.
Main Use Cases

**The main use cases for this system are:**

1.	Retrieve Share Price Data
   
2.	Store Share Price Data
   
3.	View Share Price Graph
   
4.	Compare Two Companies
   
5.	Load Offline Data
    
**Use Case Diagram:**


<img width="566" height="463" alt="image" src="https://github.com/user-attachments/assets/4ff14a8b-b811-4d50-bf8e-a504af946a51" />



















# Use Case Descriptions

Use Case 1: 
Retrieve Share Price Data
Actor: User
Purpose: To get daily share price data for a selected company between two dates.
Steps performed by the actor
1.	The user enters a share symbol, such as AAPL or TSLA.
2.	The user selects a start date.
3.	The user selects an end date.
4.	The user clicks the search or submit button.

5.	
**Steps 2**
1.	The system checks that the symbol has been entered correctly.
2.	The system checks that the date range is valid.
3.	The system makes sure the selected period does not exceed two years.
4.	The system requests share price data from an external source.
5.	The system receives the daily share price data.
6.	The system displays the data to the user.

   ---------------------------------------------------------------------

**Use Case 2:** 



Store Share Price Data

Actor: 

User
Purpose: 
To save retrieved share price data so it can be used again later, including when offline.

Steps performed by the actor
1.	The user searches for share price data.
2.	The user requests to save the data, or the system saves it automatically.


**Steps 2**
1.	The system receives the share price data.
2.	The system stores the data in local storage, such as JSON files or SQLite.
3.	The system confirms that the data has been saved successfully.
----------------------------------------------------------
**Use Case 3: View Share Price Graph**

Actor: User

Purpose: 
To display share price data as a graph over time.

Steps performed by the actor
1.	The user selects a company or previously retrieved data.
2.	The user chooses to display the graph.
3.	
**Steps 2**

1.	The system loads the required share price data.
2.	The system generates a graph using the daily price values and dates.
3.	The system displays the graph on the screen.

 --------------------------------------------------------------------------------  
**Use Case 4: Compare Two Companies**

Actor: User

Purpose: To compare the share prices of two companies on the same chart.
Steps performed by the actor
1.	The user enters the first share symbol.
2.	The user enters the second share symbol.
3.	The user selects a start date and an end date.
4.	The user submits the comparison request.

   
**Steps 2**
1.	The system checks both share symbols.
2.	The system checks that the selected date range is valid.
3.	The system retrieves share price data for both companies.
4.	The system stores the data if needed.
5.	The system generates one graph containing both companies’ prices.
6.	The system displays the comparison chart.


--------------------------------------------------------------------------


**Use Case 5: Load Offline Data**


Actor: User
Purpose: To allow the user to view saved share price data when there is no internet connection.
Steps performed by the actor
1.	The user requests share price data.
2.	If internet is unavailable, the user chooses to load saved data.

**Steps 2**
1.	The system checks whether a network connection is available.
2.	If there is no connection, the system searches local storage.
3.	The system retrieves previously saved share price data.
4.	The system displays the saved data or graph.






