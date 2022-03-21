
/* Server and Input-Output setup */
(

Server.default=s=Server.local;

//Setup according to your system



s.options.inDevice_("Analog (1+2) (RME Babyface)");

s.options.outDevice_("Altoparlanti (RME Babyface");

s.options.hardwareBufferSize_(512);
s.options.sampleRate_(44100);


s.boot;

)



/* Octaver */


(




SynthDef(\octaverMain,{

	//For simplicity I assume the knobs in the GUI will range
	//within [0, 100] but it's arbitrary

	arg inBus, outBus = 0,
	octaveUp1Bus, octaveUp2Bus,
	octaveDown1Bus, octaveDown2Bus,
	dryKnobLevel = 100,
	octaveUp1KnobLevel = 100,
	octaveUp2KnobLevel = 0,
	octaveDown1KnobLevel = 100,
	octaveDown2KnobLevel = 0;

	var inputSignal;
	var totalLevel, dryKnobNormalized,
	octaveUp1Normalized, octaveUp2Normalized,
	octaveDown1Normalized, octaveDown2Normalized;

	//Calculate signal proportions
	//to make sure the sum amplitude is always
	//constant (= 1)



	totalLevel = dryKnobLevel + octaveUp1KnobLevel
	+ octaveUp2KnobLevel +octaveDown1KnobLevel
	+ octaveDown2KnobLevel;

	dryKnobNormalized = dryKnobLevel / totalLevel;
	octaveUp1Normalized = octaveUp1KnobLevel / totalLevel;
	octaveUp2Normalized = octaveUp2KnobLevel / totalLevel;
	octaveDown1Normalized = octaveDown1KnobLevel / totalLevel;
	octaveDown2Normalized = octaveDown2KnobLevel / totalLevel;



	inputSignal = In.ar(inBus, 1);

	//Route the signal to the busses proportionally
	//to knob normalized values

	Out.ar(outBus, inputSignal*dryKnobNormalized);
	Out.ar(octaveUp1Bus, inputSignal*octaveUp1Normalized);
	Out.ar(octaveUp2Bus, inputSignal*octaveUp2Normalized);
	Out.ar(octaveDown1Bus, inputSignal*octaveDown1Normalized);
	Out.ar(octaveDown2Bus, inputSignal*octaveDown2Normalized);

}).add;


/*Pitch shifting in time domain (using PitchShift.ar) version */

SynthDef(\octaveUp1PitchShiftTimeDomain, {
	arg outBus = 0, inBus;

	var inputSource, pitchShifted;

	inputSource = In.ar(inBus, 1);

	pitchShifted = PitchShift.ar(inputSource, pitchRatio:2);

	Out.ar(outBus, pitchShifted);

}).add;


SynthDef(\octaveUp2PitchShiftTimeDomain, {
	arg outBus = 0, inBus;

	var inputSource, pitchShifted;

	inputSource = In.ar(inBus, 1);

	pitchShifted = PitchShift.ar(inputSource, pitchRatio:4);

	Out.ar(outBus, pitchShifted);

}).add;



/* Pitch shifting following the "analog approach" */


SynthDef("octaveUp1", {
	arg outBus=0, inBus;
	var lpfOut, rectSig, in;
	in = In.ar(inBus, 1);
	//We perform full wave rectification (by taking absolute value)
	//to double the frequency
	rectSig =abs(in);
	//Remove the DC component introduced by rectification
	rectSig = LeakDC.ar(rectSig, 0.999);
	//Lowpass the signal to smooth out the abrupt changes introduced by rectification
	//(This needs some manual tuning I think)
	lpfOut = LPF.ar(rectSig, 4000);


	Out.ar(outBus, lpfOut);


}).add;




SynthDef("octaveDown1", { arg outBus=0, inBus;
	var lpfOut, ff1, ff2, rectSig, in, oct1, oct2, direct, halfRect;
	in = In.ar(inBus, 1);

	//Do half-wave rectification

	halfRect = (in+abs(in))/2;
	ff1 = ToggleFF.ar(halfRect)-0.5; // use flip-flop to generate square wave an octave below, remove DC component



	Out.ar(outBus, ff1*in);
}).add;






SynthDef(\readInputSignal, {

	arg outBus = 0;

	//Adjust argument to your input port
	Out.ar(outBus, SoundIn.ar(1));

}).add;



)

(

var octaveUp1Bus = Bus.audio(s,1);
var octaveUp2Bus = Bus.audio(s, 1);
var octaveDown1Bus = Bus.audio(s,1);
var octaveDown2Bus = Bus.audio(s, 1);

var inputBus = Bus.audio(s, 1);

x = Synth(\octaveUp1, [\inBus, octaveUp1Bus]);
y = Synth(\octaveDown1, [\inBus, octaveDown1Bus]);
z = Synth(\octaverMain, [\inBus, inputBus, \octaveUp1Bus, octaveUp1Bus,
	\octaveUp2Bus, octaveUp2Bus,
	\octaveDown1Bus, octaveDown1Bus,
	\octaveDown2Bus, octaveDown2Bus
]);


h = Synth(\readInputSignal, [\outBus, inputBus]);



)




