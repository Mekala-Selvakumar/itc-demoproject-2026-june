# Fee Calculator — Jest Demo Project
## Module 3 | Building with GitHub Copilot (SR/RPS NIIT)

### Quick Start
```bash
npm install
npx jest --verbose              # run all tests
npx jest --coverage             # with coverage → coverage/lcov-report/index.html
npx jest tests/feeCalculator.test.ts --verbose    # unit tests only
npx jest tests/feeCalculatorService.test.ts       # mock tests only
```

### Project Structure
```
src/
  feeCalculator.ts           ← calculateFee() — function under test
  feeCalculatorService.ts    ← FeeCalculatorService with FeeRepository
tests/
  feeCalculator.test.ts      ← happy-path, sad-path, edge-case, parameterised, hollow examples
  feeCalculatorService.test.ts ← jest.fn() mock + toHaveBeenCalledWith() verify tests
```

### Lab Tasks (Slide 10)
| Task | Command / Prompt |
|------|-----------------|
| Generate tests | `/tests #file:feeCalculator.ts` |
| Parameterise | `#selection convert to test.each([...])` |
| Mock repository | `#selection mock FeeRepository with jest.fn()` |
| Coverage | `npx jest --coverage` |
| Review hollow | `#selection does this test verify the correct behaviour?` |
