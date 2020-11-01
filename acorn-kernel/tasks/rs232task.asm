#define UBRR_VAL	51	;9600

#define MEASUREMENT_ON   0xFF
#define MEASUREMENT_OFF   0x00

;@MACRO LSL16 MSB,LSB
.MACRO LSL16
 lsl @1
 rol @0
.ENDMACRO

;@MACRO LSR16 MSB,LSB
.MACRO LSR16
 lsr @0
 ror @1
.ENDMACRO

.dseg
status: .byte 1 
.cseg

.set rs232dpc =1

.set TEMP_MEASURE_EVENT=6		;event id for temp measure event

RS232_Task:
	sbi DDRA,PA1	;debug
	cbi PORTA,PA1


	rcall usart_init

	ldi argument,MEASUREMENT_OFF
	sts status,argument
rs232main:

	;_SLEEP_CPU_READY VOID_CALLBACK,VOID_CALLBACK,temp

	_INTERRUPT_WAIT	rs232dpc



	;start measument
	_EVENT_SET TEMP_MEASURE_EVENT,TASK_CONTEXT


	_INTERRUPT_END rs232dpc


rjmp rs232main


/*****USART Init********************
Prepare for default BT settings 
*@USAGE:temp
*/

usart_init:
	ldi temp,high(UBRR_VAL)
	out UBRRH,temp 

	ldi temp,low(UBRR_VAL)
	out UBRRL,temp

	; Enable receiver and tranciever interrupt
	ldi temp, (1 << RXCIE)|(1<<RXEN)|(1<<TXEN)
	out UCSRB,temp

	; Set frame format: 8data,NO parity check,1stop bit by default	
	ldi temp,(1 << UCSZ1) | (1 << UCSZ0);
	out UCSRC,temp

ret

/***************flash buffers********************
*/
usart_flush:
	sbis UCSRA, RXC
ret
	in temp, UDR
	rjmp usart_flush
ret

/***********Send byte in polling mode**********************
*@INPUT: argument
*/
rs232_send_byte:
	; Wait for empty transmit buffer
	sbis UCSRA,UDRE
	rjmp rs232_send_byte
	; Put data into buffer, sends the data
	out UDR,argument
ret

;*************************************************************************
;				Display byte as 1..3 digits 0..255
;check for leading zeros and remove them using T flag in SREG
;@INPUT:argument
;@USAGE:temp,argument
;STACK: 1 level
;*************************************************************************

rs232_dec_out:
	set   ;used to fascilitate leading ziro removal
	ldi temp, -1 + '0' 

rsdout1: 
	inc temp 
	subi argument, 100 
	brcc rsdout1
;write out first digit
	push argument
	mov argument,temp
;no need of leading ziro
	cpi argument,'0'
	breq rsdout11 
	rcall rs232_send_byte
	clt 

rsdout11:		 
	pop argument

	ldi temp, 10 + '0' 

rsdout2: 
	dec temp 
	subi argument, -10 
	brcs rsdout2
	sbci argument, -'0' 
;write out second digit
	push argument
	mov argument,temp           
;test for leading zero - if T is clear stop testing - it is not leading zero
	brtc rsdout222
	cpi argument,'0'		 
	breq rsdout22         

rsdout222:		 
	rcall rs232_send_byte

rsdout22:		 
	pop argument
;write out third digit
	rcall rs232_send_byte
ret 

RxComplete:
_PRE_INTERRUPT	
 	in temp, UDR	;dummy read
	lds temp,status	
	cpi temp,MEASUREMENT_OFF
	breq start_measurement
	
_POST_INTERRUPT
reti

start_measurement:
	ldi temp,MEASUREMENT_ON				;bring status to 0xFF/busy
	sts status,temp
_keDISPATCH_DPC rs232dpc

.EXIT