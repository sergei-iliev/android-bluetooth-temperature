 /*
@WARNING - IN WORKING PROGRESS
SLEEP mode.
Integrating SLEEP mode in a multitasking kernel is a bit tricky by itself.
CPU could either be I/O bound or algorithm bound which may require to finish
before the sleep command is carried out. 
It is required that some/all interrupt processing tasks needs to know when a
sleep mode is about to begin so that they could finish/turn off unnessesary interrupt 
activity.
Current sleep implementation is based on
1.SLEEP is NOT possible in high DEVICE level.
2.SLEEP is executed in USER level from System task 
*/	

 /*
 *SLEEP structure 
 *  |x|x|x|x|x|x|x|x|
 *  x -> count number of tasks ready to sleep
 *  If x=0 no sleep is requested
 *  If x=1 a task has requested a sleep, the other tasks must follow in the sleep sequance
 *  If x=SLEEP_TASKS_REQUEST_NUMBER all tasks are ready to sleep , fire SLEEP instruction
 */

 ;set the number of all tasks to participate in the sleep
.SET SLEEP_TASKS_REQUEST_NUMBER=1

.SET VOID_CALLBACK=0

 .dseg
	sleepmode:	  .byte 1
 .cseg

  /********************************************
  *Initialization routine
  *Called in init routine of the task before other task body execution
  *Usually system task is the one to handle sleep
  *@INPUT: 0-> temp register
  *********************************************/
 .MACRO _SLEEP_CPU_INIT

	;disable USI
	in @0,PRR
	sbr @0,(1<<PRUSI)
    out PRR,@0

	;disable ADC
	in @0,ACSR
	cbr @0,(1<<ACIE)
	sbr @0,(1<<ACD)
	out ACSR,@0

	lds @0,sleepmode
	clr @0
	sts sleepmode,@0

	
 .ENDMACRO

/************************************************
*Main point to request SLEEP
*Single task designated as MAIN TASK can fire it up
*One point/line of use for the whole app!!!!!!!!!!!!!!!!!
*Pre sleep and post sleep callbacks execute sleep related code - like disabling task's interrupt
*@INPUT: 0-> pre sleep callback to execute  
*	     1-> post sleep callback to execute	
         2-> temp register to be used
*@USAGE:
*		 Z
***********************************************/
 .MACRO _SLEEP_CPU_REQUEST	
	ldi ZL,low(@0)
	ldi ZH,high(@0)
	
	CPI16 ZL,ZH,temp,VOID_CALLBACK    ;empty callback -> skip
    breq prevoid
	icall
prevoid: 
	 
	cli
	lds @2,sleepmode
	sbr @2,(1<<0)	;set counter to 1
	sts sleepmode,@2
	sei
	
	;loop until sleep is finished
macro_loop:
    _YIELD_TASK
					
	lds @2,sleepmode
	tst @2
	brne macro_loop    ;-> _SLEEP is expected to set to 0 counter x

	;post callback execute
	ldi ZL,low(@1)
	ldi ZH,high(@1)
	
	CPI16 ZL,ZH,temp,VOID_CALLBACK    ;empty callback -> skip
    breq postvoid
	icall
	
postvoid:
 .ENDMACRO

/************************************************
*Task to participate in a sleep.Pre sleep and post sleep callbacks
* execute sleep related code - like disabling task's interrupt
*@INPUT: 0-> pre sleep callback to execute  
*	     1-> post sleep callback to execute	
         2-> temp register to be used		 
*@USAGE:
*		 Z
***********************************************/
 .MACRO _SLEEP_CPU_READY
	//check if sleep is requested
	;lds @2,sleepmode
	;tst @2
	;breq endmacro

	
	ldi ZL,low(@0)
	ldi ZH,high(@0)
	
	CPI16 ZL,ZH,temp,VOID_CALLBACK    ;empty callback -> skip
    breq prevoid
	icall
prevoid: 
	
	
	;increament sleep task counter
	cli	
	lds @2,sleepmode
	tst @2
	brne macro_loop_0
	ldi @2,1
	rjmp macro_loop_1
macro_loop_0:
	inc @2
macro_loop_1:		
	sts sleepmode,@2
	sei
	
	;loop until sleep is finished
macro_loop:
    _YIELD_TASK	
	lds @2,sleepmode
	tst @2
	brne macro_loop    ;-> _SLEEP is expected to set to 0 counter x

	;post callback execute
	ldi ZL,low(@1)
	ldi ZH,high(@1)
	
	CPI16 ZL,ZH,temp,VOID_CALLBACK    ;empty callback -> skip
    breq postvoid
	icall
postvoid:

endmacro:		
 .ENDMACRO


/************************************************
* Puts CPU in SLEEP IDLE mode
* Single place of call - usually in system task
*@INPUT: 0-> temp register  
*************************************************/
 .MACRO _SLEEP_CPU
 
	lds @0,sleepmode	
	cpi @0,SLEEP_TASKS_REQUEST_NUMBER
	brne endmacro     ; still waiting for other tasks to request sleep
 
	_DISABLE_TASKSHEDUAL_TIMER

	in @0,MCUCR
	cbr @0,(1<<SM0)|(1<<SM1)
	sbr @0,(1<<SE)       ;SM2=0;SM1=0;SM0=0=>IDLE MODE 
	out MCUCR,@0
				
	sleep
	
	;reset counter to 0
	cli		
	lds @0,sleepmode
	clr @0
	sts sleepmode,@0    
	_ENABLE_TASKSHEDUAL_TIMER @0		
	sei
	

endmacro:
 .ENDMACRO


/************************************************
;Check if CPU is sleeping
;Called in INTERRUPT context
;@INPUT: 
0-> jump {YES}if sleeping
1-> jump {NO}to if NOT sleeping
2-> temp reg
*************************************************/
.MACRO _IS_CPU_SLEEPING
	lds @2,sleepmode		
	tst @2
	brne @0
	rjmp @1
.ENDMACRO 
