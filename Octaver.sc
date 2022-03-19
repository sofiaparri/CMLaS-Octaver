
/* Server and Input-Output setup */
(

s.boot;



//List devices

ServerOptions.devices;

Server.default.options.inDevice_("Analog (1+2) (RME Babyface)");

Server.default.options.outDevice_("Altoparlanti (RME Babyface");


//Server.default.options.hardwareBufferSize = 512;

)



/* Octaver */


(

var octaveUp1Bus = Bus.audio(s,1);
var octaveUp2Bus = Bus.audio(s, 1);

var inputBus = Bus.audio(s, 1);


SynthDef(\octaverMain,{

	//For simplicity I assume the knobs in the GUI will range
	//within [0, 100] but it's arbitrary

	arg inBus, outBus = 0,
	octaveUp1Bus, octaveUp2Bus,
	dryKnobLevel = 0,
	octaveUp1KnobLevel = 100,
	octaveUp2KnobLevel = 0;

	var inputSignal;
	var totalLevel, dryKnobNormalized,
	octaveUp1Normalized, octaveUp2Normalized;

	//Calculate signal proportions
	//to make sure the sum amplitude is always
	//constant (= 1)



	totalLevel = dryKnobLevel + octaveUp1KnobLevel
	+ octaveUp2KnobLevel;

	dryKnobNormalized = dryKnobLevel / totalLevel;
	octaveUp1Normalized = octaveUp1KnobLevel / totalLevel;
	octaveUp2Normalized = octaveUp2KnobLevel / totalLevel;



	inputSignal = In.ar(inBus, 1);

	//Route the signal to the busses proportionally
	//to knob normalized values

	Out.ar(outBus, inputSignal*dryKnobNormalized);
	Out.ar(octaveUp1Bus, inputSignal*octaveUp1Normalized);
	Out.ar(octaveUp2Bus, inputSignal*octaveUp2Normalized);

}).add;

SynthDef(\octaveUp1, {
	arg outBus = 0, inBus;

	var inputSource, pitchShifted;

	inputSource = In.ar(inBus, 1);

	pitchShifted = PitchShift.ar(inputSource, pitchRatio:2);

	Out.ar(outBus, pitchShifted);

}).add;


SynthDef(\octaveUp2, {
	arg outBus = 0, inBus;

	var inputSource, pitchShifted;

	inputSource = In.ar(inBus, 1);

	pitchShifted = PitchShift.ar(inputSource, pitchRatio:4);

	Out.ar(outBus, pitchShifted);

}).add;



SynthDef(\readInputSignal, {

	arg outBus = 0;

	//Adjust argument to your input port
	Out.ar(outBus, SoundIn.ar(1));

}).add;



x = Synth(\octaveUp1, [\inBus, octaveUp1Bus]);
y = Synth(\octaveUp2, [\inBus, octaveUp2Bus]);
z = Synth(\octaverMain, [\inBus, inputBus, \octaveUp1Bus, octaveUp1Bus,
	\octaveUp2Bus, octaveUp2Bus
]);


h = Synth(\readInputSignal, [\outBus, inputBus]);






)




