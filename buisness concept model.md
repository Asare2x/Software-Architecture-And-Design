# Buisness concept model
 
 **Sprint 2:**


Introduction

The Business Concept Model describes main concepts involved in the Share Price Comparison Web Application. 
It focuses on the key parts of the system and how they are related to each other from a business point of view. 
The model helps to understand how users interact with the system to search for share prices, view charts, and compare company shares over time.


**Main Business Concepts**

User  /invester
The user, also called the investor, is the person who uses the application. The user searches for share prices, selects a date range, views charts, compares shares, and exports data.


**Company Share**


A company share represents a company listed on the stock market. Users can view historical price information for the company’s shares.


**Share Symbol**


A share symbol is a short code used to identify a company share in the stock market. For example, companies like Apple or Tesla have unique symbols that are used to search for their share price data.




**Price Data**

Price data represents the historical daily price of a company’s share. Each price record contains a specific date and the share price for that day.




**Date Range**


The date range is selected by the user to define the time period for viewing share prices. The system allows users to retrieve price data within a maximum range of two years.


**Price Chart**



A price chart is a visual graph that shows how the share price changes over time. The chart helps users easily understand the share performance.







**Share Comparison**



The system allows users to compare the price performance of two different company shares over the same date range.






**Cached Data**



Cached data refers to share price data that is stored locally by the system. This allows the application to still display previously saved data even when there is no internet connection.











**Simple Moving Average**


The Simple Moving Average is a calculated value that helps users see the general trend of the share price over time. It can be shown as an optional overlay on the chart.









**Exported Data**
Exported data allows users to save or download the share price data for later use.



# Relationships Between Business Concepts:








User searches for a Share Symbol to find a company share.

•	 Share Symbol identifies a Company Share.

•	 User selects a Date Range to define the time  period for viewing share prices.

•	The system retrieves Price Data for the selected share and date range.

•	The Price Data is displayed in a Price Chart.

•	User can compare two Company Shares using the same chart.

•	The system can calculate a Simple Moving Average based on the price data.

•	The system may store Price Data as Cached Data so it can be accessed later without an internet connection.

•	User can export or save the share price information as Exported Data.














**Business Concept Diagram**

**The relationships between the main concepts in the system are described below:**

<img width="940" height="667" alt="image" src="https://github.com/user-attachments/assets/1096e503-c0a6-4f51-9abc-f35d0c9dd3ac" />












**Summary**

The business concept model gives a clear overview of the main concepts involved in the Share Price comparison web application. It shows how users interact with company share data, select date ranges, view price charts, and compare shares. the model also highlights additional system capabilities such as caching price data for offline use, calculating moving averages, and exporting share price information. this model helps guide the development of the system architecture and ensures that the system supports the main user requirements.





