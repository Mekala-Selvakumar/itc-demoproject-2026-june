# Fee Calculator — Pytest Demo Project
## Module 3 | Building with GitHub Copilot (SR/RPS NIIT)

### Quick Start
```bash
pip install -r requirements.txt
pytest -v                                        # run all tests
pytest --cov=src --cov-report=term-missing       # with coverage
pytest tests/test_fee_calculator.py -v           # unit tests only
pytest tests/test_fee_parametrized.py -v         # parameterised + mocks
```

### Project Structure
```
src/
  fee_calculator.py          ← calculateFee() — function under test
  fee_calculator_service.py  ← FeeCalculatorService with FeeRepository
tests/
  conftest.py                ← fixtures + test data table
  test_fee_calculator.py     ← happy-path, sad-path, edge-case, hollow examples
  test_fee_parametrized.py   ← parameterised + mock tests
```

### Lab Tasks (Slide 10)
| Task | Command |
|------|---------|
| Generate tests | `/tests #file:fee_calculator.py` |
| Parameterise | `#selection convert to @pytest.mark.parametrize` |
| Mock repo | `#selection mock FeeRepository.save()` |
| Coverage | `pytest --cov=src --cov-report=term-missing` |
| Review hollow | `#selection does this test verify correct behaviour?` |
