/*
12 bit resolution const
*/
	#define ow9bit 0x5;0.5°C, 
	#define ow10bit 0x19;0.25°C, 
	#define ow11bit 0x7D;0.125°C,
	#define ow12bit 0x271;0.0625°C

	#define CONFIG_10BIT_RESOLUTION 0x3F
	#define CONFIG_12BIT_RESOLUTION	0x7F ;default
;DS18B20 ROM commands
	#define SEARCH_ROM 0xF0
	#define READ_ROM 0x33
	#define MATCH_ROM 0x55
	#define SKIP_ROM 0xCC
	#define ALARM_SEARCH 0xEC

;DS18B20 Function Commands 
	#define CONVERT_T 0x44
	#define WRITE_SCRATCHPAD 0x4E
	#define READ_SCRATCHPAD 0xBE
	#define COPY_SCRATCHPAD 0x48
	#define READ_POWER_SUPPLY 0xB4

	#define IDLE_DELAY 20

.set TIMEOUT_EVENT=7

.equ OW_PIN	= PB4 

.equ OW_OUT = PORTB
.equ OW_DIR = DDRB
.equ OW_IN=PINB

;PD7

.dseg
ocsr2:	  .byte 1

;DS18B20 ROM pad

ROM:
code:     .byte 1
number:	 .byte 6
crc:	 .byte 1
	
;DS18B20 Scratch pad	

PAD:
TL:  .byte 1
TH:  .byte 1
none: .byte 6
padcrc: .byte 1

.cseg

/**************One Wire Init********************************** 
*
***************************************************************/
ow_init:
	
	cbi OW_OUT,OW_PIN	;disable pull up
	cbi OW_DIR,OW_PIN   ;input 
	
	rcall inittimer

ret

inittimer:
// Enable CTC mode (mode 2); TCNT0 counts from 0 to OCR0A inclusive
// Prescaler CLKio/8 = 1 us resolution
	in  temp,TCCR1A
	sbr temp,(0<<WGM13)|(0<<WGM12)|(0<<WGM11)|((0<<WGM10))
	out TCCR1A,temp

	in temp,TCCR1B
	sbr temp,(1<<WGM12)|(1<<CS11 )
	out TCCR1B,temp
	// Start counting from 0
	clr temp	
    out TCNT1H,temp
    out TCNT1L,temp
ret

starttimer:
	;perge event
	_EVENT_RESET TIMEOUT_EVENT

	in temp,TIMSK
	sbr temp,(1<<OCIE1A)
	out TIMSK,temp

ret

stoptimer:
	in  temp,TCCR1B
	cbr temp,(1<<CS12) | (1<<CS11 )|(1<<CS10)
	out TCCR1B,temp

	in temp,TIMSK
	cbr temp,(1<<OCIE1A)
	out TIMSK,temp

ret

medtimer:
	rcall stoptimer

	// Reset counter, so start counting from the moment the timer is re-enabled
	// Start counting from 0
	clr temp	
    out TCNT1H,temp
    out TCNT1L,temp

	lds temp,ocsr2
	out OCR1AL,temp

	rcall starttimer

	in  temp,TCCR1B
	sbr temp,(1<<CS11) | (1<<CS10 )				;64 devider
	cbr temp,(1<<CS12)
	out TCCR1B,temp

ret
/**************One Wire Reset********************************** 
*
*  Reset devices on the bus. Wait for the reset process to complete.
*  Return 1 if there are devices on the bus, else 0.
@USAGE:   temp
@OUTPUT:  T flag 0 -> yes, ds18b20 is present
		  T flag 1 -> no device	
***************************************************************/

ow_reset:
	_DISABLE_TASK_SWITCH TRUE
	set  ;no device
	sbi	OW_DIR, OW_PIN		; Bus Low 
    
	ldi temp,60;70
	sts ocsr2,temp				 
	rcall	medtimer		; Wait about 500us 
	_EVENT_WAIT TIMEOUT_EVENT
	
	cbi	OW_DIR, OW_PIN		; Bus High
	
	ldi temp,10  
	sts ocsr2,temp
	rcall	medtimer		; Wait about 100us  
	_EVENT_WAIT TIMEOUT_EVENT
	
	;read result
	in	temp, OW_IN 
	bst	temp, OW_PIN		; Store bus status 

	ldi temp,60  
	sts ocsr2,temp
	rcall	medtimer		; Wait about 500us  
	_EVENT_WAIT TIMEOUT_EVENT

	_DISABLE_TASK_SWITCH FALSE

ret


/******************Write One Bit*********************
@INPUT:C - status bit
@USAGE: temp 
****************************************************/

ow_write_bit:
	brcc owwriteb_0

owwriteb_1:
	;1<t<15us
	sbi	OW_DIR, OW_PIN		; Bus low ( 1us to 15us ) 
	rcall ow_wait_2us
	rcall ow_wait_2us
	rcall ow_wait_2us
	rjmp owwriteb_end 

owwriteb_0:
	;60<t<120
	sbi	OW_DIR, OW_PIN	;bus low
	
	ldi temp,10;6    ;80us
	sts ocsr2,temp
	rcall	medtimer		; Wait more 60us<t<120us  
	_EVENT_WAIT TIMEOUT_EVENT

owwriteb_end:
	cbi	OW_DIR, OW_PIN	    ;bus high for end of slot
	;60 us
	ldi temp,6  
	sts ocsr2,temp
	rcall	medtimer		; Wait about 60us  
							
	_EVENT_WAIT TIMEOUT_EVENT
	nop

ret
/********************One Wire Write Byte*********
@INPUT: argument - byte to write
@USAGE: temp,counter 
*************************************************/

ow_write:
	_DISABLE_TASK_SWITCH TRUE
	ldi	counter, 8			    ; 8 bits to write 

owwrite_loop:
	ror	argument
	rcall ow_write_bit

	dec counter
	tst counter
	brne owwrite_loop

	_DISABLE_TASK_SWITCH FALSE
ret

/********************One Wire Read Byte*********
@OUTPUT: return
@USAGE: temp,counter 
*************************************************/

ow_read:
	_DISABLE_TASK_SWITCH TRUE
	ldi	counter, 8			    ; 8 bits to write 
	clr return

owread_loop: 
	sbi	OW_DIR, OW_PIN		; Bus low ( 1us to 15us ) 
	rcall ow_wait_2us
	rcall ow_wait_2us
	rcall ow_wait_2us

	cbi	OW_DIR, OW_PIN		; Bus high 
	rcall ow_wait_2us
	rcall ow_wait_2us
	rcall ow_wait_2us
	; Get Data Now 

	lsr	return 
	sbic	OW_IN, OW_PIN		; check bit 
	sbr	return, 0x80
	
	
	;80 us
	ldi temp,10;6  
	sts ocsr2,temp
	rcall	medtimer		; Wait about 80us  
							
	_EVENT_WAIT TIMEOUT_EVENT
									 
	dec	counter 
	breq owread_loop_end
	rjmp owread_loop 

owread_loop_end:	 
	_DISABLE_TASK_SWITCH FALSE

ret

/********************One Wire Read ROM***********
@USAGE: axl,Y,return,argument
@CALL: 
@OUTPUT:  T flag 0 -> yes, crc OK
		  T flag 1 -> crc failure
*************************************************/

ow_read_rom:
	ldi argument,READ_ROM
	rcall ow_write

	
	ldi	YH,high(ROM) 
	ldi	YL,low(ROM)		;init Y-pointer 

	ldi axl,8			;8 bytes rom pad

rrloop:   
	rcall ow_read
	st Y+,return

	dec axl
	brne rrloop

	;run crc check
	ldi	YH,high(ROM) 
	ldi	YL,low(ROM)		;init Y-pointer 
	
	clr return			;global for sub routine
	ldi axl,7

crcloop:
	ld argument,Y+
	rcall ow_crc8
	dec axl
	brne crcloop


	;check validity
	lds argument,crc
	cp argument,return
	brne  exit
	clt ;crc success

ret

exit:
	set  ;crc failure    

ret

/*********************One Wire Temp Conv*****************
@INPUT:   
@USAGE: argument
*****************************************************/

ow_temp_conv:
	ldi	argument, SKIP_ROM		; Skip ROM check 
	rcall	ow_write 

	ldi	argument, CONVERT_T		; Start Temp Conversion 
	rcall	ow_write 

tcwait: 
	sbis	OW_IN, OW_PIN		; Conversion Done! 
	rjmp	tcwait 

ret

/*********************One Wire Read Scratch Pad*****************
@USAGE: axl,Y,return,argument
@CALL: 
@OUTPUT:  T flag 0 -> yes, crc OK
		  T flag 1 -> crc failure
*****************************************************/

ow_read_pad:
    clt
	ldi	argument, SKIP_ROM		; Skip ROM check 
	rcall ow_write 

	ldi	argument, READ_SCRATCHPAD		; read Scratch Pad 
	rcall ow_write

	ldi	YH,high(PAD) 
	ldi	YL,low(PAD)		;init Y-pointer 

	ldi axl,9			;8 bytes rom pad

rploop:   
	rcall ow_read
	st Y+,return
	dec axl
	brne rploop
ret  ;;shortcut!!!!!!!!!!!!!!!!

	;run crc check
	ldi	YH,high(PAD) 
	ldi	YL,low(PAD)		;init Y-pointer 
	
	clr return			;global for sub routine
	ldi axl,7

rpcrcloop:
	ld argument,Y+
	rcall ow_crc8
	dec axl
	brne rpcrcloop

	;check validity
	lds argument,crc
	cp argument,return
	brne  rpexit
	clt ;crc success

ret

rpexit:
	set  ;crc failure    

ret

/*********************One Wire Write Scratch Pad*****************
;Chnage configuration byte only!
@INPUT:bxl
@USAGE: argument
*****************************************************/
ow_write_pad:
	ldi	argument, SKIP_ROM		; Skip ROM check 
	rcall ow_write
	
	ldi	argument, WRITE_SCRATCHPAD		; write Scratch Pad 
	rcall ow_write
	
	mov argument,bxl
	rcall ow_write 
    
	mov argument,bxl
	rcall ow_write

	mov argument,bxl
	rcall ow_write
ret


/*********************One Wire CRC-8*****************
@INPUT:   argument,return(accumulated crc)
@USAGE: temp
*****************************************************/

ow_crc8:
	push	argument			; Must save for next bit 
	ldi	temp, 0x08				; 8 bits 

crc8_loop: 
	eor	argument, return		; 
	ror	argument 
	mov	argument, return		; 
	brcc	crc8_skip		; Skip if zero 
	push	temp 
	ldi	temp, 0x18		; 
	eor	argument, temp 
	pop	temp			; 
 

crc8_skip: 
	ror	argument 
	mov	return, argument 
	 
	pop	argument 
	lsr	argument			; Align bits 
	push	argument 
	dec	temp 
	brne	crc8_loop		; Process 8 bits 
	pop	argument			; Clean up stack 

ret


//**********************MIND THE CPU Frequency************
// 1us delay
//********************************************************

ow_wait_2us:               
	nop
	nop
	nop
	nop
	nop
	nop
	nop
	nop
	nop
ret  
