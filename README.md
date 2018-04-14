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

[path_to_market_data_file] + [amount]

### Run (with large java command)
>  java -jar ./build/libs/loan_provider-1.0.jar ./market.csv 900

### Run (with bash)
>  ./run.sh ./market.csv 900

### Assumptions

* Console application to manage a low / medium size of lenders. (synchronized reading)
* Use BigDecimal for all money values.
* The market data file is never damaged (it still detects exceptions).
* The market data file is never damaged (it still detects exceptions).
* Money-Weighted Rate in order to each lender amount.