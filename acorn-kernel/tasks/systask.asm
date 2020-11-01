

System_Task:
  _SLEEP_CPU_INIT temp

sys:

	_SLEEP_CPU temp

	_YIELD_TASK
rjmp sys
