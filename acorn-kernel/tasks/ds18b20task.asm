/*
 * ds18b20.asm driver at 8MHz
 *
 */

.def    argument=r17    
.def    return=r18

.def    axl=r19  
.def    axh = r20
.def    bxl=r21
.def    bxh=r22
.def    counter=r23

.include "tasks/ds18b20op.asm"
;----------------------------------------------------------------DEBUG TASK-------------------------------------------
/*
DEBUG task to fetch ROM content
*/
DEBUG_DS18B20_Task:  	
	rcall ow_init
dtt_init:
    _SLEEP_TASK 2
	rcall ow_reset
	brts dtt_init    ;no presence

	;change resolution to 10bit
    ldi bxl,CONFIG_10BIT_RESOLUTION
	rcall ow_write_pad
debug_main:

 	
	_SLEEP_TASK 250
	_SLEEP_TASK 250
	_SLEEP_TASK 250
	_SLEEP_TASK 250
	_SLEEP_TASK 250
	_SLEEP_TASK 250
	rcall ow_reset

	rcall ow_read_rom
	;brts dtt_fail    ;failure
	
	ldi	YH,high(number) 
	ldi	YL,low(number)		;init Y-pointer 
	


	rcall send_char
	
	rcall send_char

	rcall send_char

	rcall send_char

	rcall send_char

	rcall send_char

	ldi argument,-1
	rcall rs232_send_byte 

	rjmp debug_main

dtt_fail:    
	ldi argument,-1
	rcall rs232_send_byte 

rjmp debug_main

send_char:
	ld argument,Y+
	rcall rs232_send_byte 
	_SLEEP_TASK 250

ret
;----------------------------------------------------------------DEBUG TASK-------------------------------------------
/*
Trigger temp measurement on each signal from RS232
The rest of the time fall to sleep 
*/
DS18B20_Task:  	 	    	
	rcall ow_init
tt_init:
    _SLEEP_TASK 2
	rcall ow_reset
	brts tt_init    ;no presence

	;change resolution to 10bit
    ldi bxl,CONFIG_10BIT_RESOLUTION
	rcall ow_write_pad


temp_task_main:
	sbi PORTA,PA1
    _SLEEP_CPU_READY VOID_CALLBACK,VOID_CALLBACK,temp
	cbi PORTA,PA1
	rcall ow_reset
	;brts tt_init    ;no presence
	rcall ow_temp_conv

	rcall ow_reset
	;brts tt_init    ;no presence
	rcall ow_read_pad
	;brts tt_init    ;wrong temp read

	rcall ow_reset
	;brts tt_init    ;no presence
	rcall ow_temp_conv

	rcall ow_reset
	;brts tt_init    ;no presence
	rcall ow_read_pad
	;brts tt_init    ;wrong temp read

	
    ;_EVENT_WAIT TEMP_MEASURE_EVENT
	rcall rx232_temp_send

	

rjmp temp_task_main

/*****************Send temperature reading over RX232*************************
*USAGE:axh,axl,argument
******************************************************************************/
rx232_temp_send:

	lds axh,TH
	;isolate sign
	andi axh,0b00000111
	
	lds axl,TL
    ;isolate 4 decimal steps
	LSR16 axh,axl	
	LSR16 axh,axl
	LSR16 axh,axl	
	LSR16 axh,axl

	mov argument,axl
	;lds argument,DebugCounter
	rcall rs232_dec_out

    ;Send line feed char to Android
    ldi argument,0x0D
	rcall rs232_send_byte 

    ldi argument,0x0A
	rcall rs232_send_byte 

	ldi temp,MEASUREMENT_OFF				;bring status to 0x00/free
	sts status,temp
ret

OC1Int:
	_PRE_INTERRUPT
	rcall stoptimer
	_EVENT_SET TIMEOUT_EVENT,INTERRUPT_CONTEXT
	_POST_INTERRUPT
	reti
