# Fee Calculator — JUnit 5 Demo Project
## Module 3 | Building with GitHub Copilot (SR/RPS NIIT)

### Quick Start
```bash
mvn test                          # run all tests
mvn test jacoco:report            # with coverage → target/site/jacoco/index.html
mvn test -Dtest=FeeCalculatorTest # unit tests only
mvn test -Dtest=FeeCalculatorServiceTest  # mock tests only
```

### Project Structure
```
src/main/java/com/finance/fee/
  service/FeeCalculator.java         ← calculateFee() — function under test
  service/FeeCalculatorService.java  ← service layer with FeeRepository
  model/Customer.java                ← domain model
  model/FeeRecord.java               ← result DTO
  repository/FeeRepository.java      ← interface (mocked in tests)

src/test/java/com/finance/fee/service/
  FeeCalculatorTest.java             ← happy-path, sad-path, edge-case, parameterised
  FeeCalculatorServiceTest.java      ← Mockito mock + verify tests
```

### Lab Tasks (Slide 10)
| Task | Command / Prompt |
|------|-----------------|
| Generate tests | `/tests #file:FeeCalculator.java` |
| Parameterise | `#selection convert to @ParameterizedTest + @CsvSource` |
| Mock repo | `#selection mock FeeRepository with @Mock and verify()` |
| Coverage | `mvn test jacoco:report` |
| Review hollow | `#selection does this test verify the correct behaviour?` |
