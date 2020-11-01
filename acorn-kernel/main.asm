.include "tn2313Adef.inc"
.include "16bitMath.inc"


.include "INTERRUPTS.inc" 
.include "Kernel.inc"
.include "SLEEP.inc"

.cseg
.def    temp=r16    ;temp reg.

.include "HARDWARE.inc"
RESET:     
    /*RAM is up to 256 bytes*/ 
    ldi     temp,low(RAMEND-2)
    out     SPL,temp

           
;clear SRAM
	ldi XL,low(RAMEND+1)
	ldi XH,high(RAMEND+1)    		   
    clr r0
initos:
	st -X,r0
	cpi XH,high(SRAM_START) 
    brne initos
    cpi XL,low(SRAM_START)
	brne initos
	 

	_REGISTER_TASK_STACK RS232_Task,1,36  
	_REGISTER_TASK_STACK DS18B20_Task,2,40 ;DEBUG_DS18B20_Task,2,40
	_REGISTER_TASK_STACK System_Task,3,36 


_START_SCHEDULAR

.include "tasks/rs232task.asm"
.include "tasks/ds18b20task.asm"
.include "tasks/systask.asm"

DispatchDPCExtension:

TaskSchedular:

;is schedular suspended?    
	_keSKIP_SWITCH_TASK task_switch_disabled

	_keOS_SAVE_CONTEXT
;start LIMBO state 
    
	_keSWITCH_TASK

;end LIMBO state
	_keOS_RESTORE_CONTEXT

task_switch_disabled:         ;no task switching

reti

SystemTickInt:
  _PRE_INTERRUPT   
   
  _kePROCESS_SLEEP_INTERVAL	
  
  _POST_INTERRUPT
rjmp TaskSchedular

.EXIT
