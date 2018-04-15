# Loan calculator
Zopa Loan Provider

### Requirements
* Java 8
* Setting the JAVA_HOME Variable

### Test
>  ./gradlew check

### Build
>  ./gradlew build

### Parameters to run

__[path_to_market_data_file]__: path to get market data from CVS file.  
__[amount]__: request to calculate a loan.

### Run (with large java command)
>  java -jar ./build/libs/loan_provider-1.0.jar ./market.csv 900

### Run (with bash)
>  ./run.sh ./market.csv 900

### Assumptions

* Console application to manage a low / medium size of lenders. (synchronized reading)
* Use BigDecimal for all money values.
* To calculate monthly payment use French method with payment of constant quotas.
* Money-Weighted Rate in order to each lender amount.
* App do not update the file with the available amount. Assumes delegate to external system.
