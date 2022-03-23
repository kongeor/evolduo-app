var abc_bak = "T: Cooley's\n" +
			"%%barnumbers 1\n" +
			"M: 4/4\n" +
			"L: 1/8\n" +
			"R: reel\n" +
			"K: Emin\n" +
			"EBBA B2 EB|~B2 AB dBAG|FDAD BDAD|FDAD dAFD|\n" +
			"EBBA B2 EB|B2 AB defg|afe^c dBAF|DEFD E2 gf|\n" +
			"|eB B2 efge|eB B2 gedB|A2 FA DAFA|A2 FA defg|\n" +
			"eB B2 eBgB|eB B2 defg|afe^c dBAF|DEFD E2 D2||";
		var NUM_MEASURES = 16;
		var NUM_BEATS_PER_MEASURE = 4;
		var synth = new ABCJS.synth.CreateSynth();
		var startMeasure;
		var endMeasure;
		var timingCallbacks;

		function load() {
			// First draw the music - this supplies an object that has a lot of information about how to create the synth.
			// NOTE: If you want just the sound without showing the music, use "*" instead of "paper" in the renderAbc call.
			var visualObj = ABCJS.renderAbc("paper", abc, {
				responsive: "resize" })[0];

			var startAudioButton = document.querySelector(".activate-audio");
			var stopAudioButton = document.querySelector(".stop-audio");
			var explanationDiv = document.querySelector(".suspend-explanation");
			startMeasure = document.querySelector("#start-measure");
			endMeasure = document.querySelector("#end-measure");

			startAudioButton.addEventListener("click", function() {
				// startAudioButton.setAttribute("style", "display:none;");
				explanationDiv.setAttribute("style", "opacity: 0;");
				if (ABCJS.synth.supportsAudio()) {
					// stopAudioButton.setAttribute("style", "");

					// An audio context is needed - this can be passed in for two reasons:
					// 1) So that you can share this audio context with other elements on your page.
					// 2) So that you can create it during a user interaction so that the browser doesn't block the sound.
					// Setting this is optional - if you don't set an audioContext, then abcjs will create one.
					window.AudioContext = window.AudioContext ||
						window.webkitAudioContext ||
						navigator.mozAudioContext ||
						navigator.msAudioContext;
					var audioContext = new window.AudioContext();
					audioContext.resume().then(function () {
						// In theory the AC shouldn't start suspended because it is being initialized in a click handler, but iOS seems to anyway.

						synth.init({
							audioContext: audioContext,
							visualObj: visualObj
						}).then(function () {
							timingCallbacks = new ABCJS.TimingCallbacks(visualObj, {
								beatCallback: cursorControl.onBeat,
								eventCallback: cursorControl.onEvent
							});
							cursorControl.onStart();
							synth.prime().then(function () {
								var start = (startMeasure.value - 1) / NUM_MEASURES;
								synth.seek(start);
								timingCallbacks.setProgress(start);
								synth.start();
								timingCallbacks.start();
							});
						}).catch(function (error) {
							console.log("Audio Failed", error);
						});
					});
				} else {
					var audioError = document.querySelector(".audio-error");
					audioError.setAttribute("style", "");
				}
			});

			stopAudioButton.addEventListener("click", function() {
				startAudioButton.setAttribute("style", "");
				explanationDiv.setAttribute("style", "");
				// stopAudioButton.setAttribute("style", "display:none;");
				synth.stop();
				timingCallbacks.stop();
			});
		}

		function CursorControl() {
		    console.log('finished?' )
			var self = this;

			self.onStart = function() {
				var svg = document.querySelector("#paper svg");
				var cursor = document.createElementNS("http://www.w3.org/2000/svg", "line");
				cursor.setAttribute("class", "abcjs-cursor");
				cursor.setAttributeNS(null, 'x1', 0);
				cursor.setAttributeNS(null, 'y1', 0);
				cursor.setAttributeNS(null, 'x2', 0);
				cursor.setAttributeNS(null, 'y2', 0);
				svg.appendChild(cursor);

			};
			self.onBeat = function(beatNumber, totalBeats, totalTime) {
				console.log(beatNumber)
				var end = (endMeasure.value-1) * NUM_BEATS_PER_MEASURE;
				if (beatNumber >= end) {
					var start = (startMeasure.value-1) / NUM_MEASURES;
					synth.seek(start);
					timingCallbacks.setProgress(start);
				}
			};
			self.onEvent = function(ev) {
			    console.log('cursor event', ev)
			    if (!ev)
			        return;
				if (ev.measureStart && ev.left === null)
					return; // this was the second part of a tie across a measure line. Just ignore it.

				var lastSelection = document.querySelectorAll("#paper svg .highlight");
				for (var k = 0; k < lastSelection.length; k++)
					lastSelection[k].classList.remove("highlight");

				for (var i = 0; i < ev.elements.length; i++ ) {
					var note = ev.elements[i];
					for (var j = 0; j < note.length; j++) {
						note[j].classList.add("highlight");
					}
				}

				var cursor = document.querySelector("#paper svg .abcjs-cursor");
				if (cursor) {
					cursor.setAttribute("x1", ev.left - 2);
					cursor.setAttribute("x2", ev.left - 2);
					cursor.setAttribute("y1", ev.top);
					cursor.setAttribute("y2", ev.top + ev.height);
				}
			};
			self.onFinished = function() {
			    console.log('finished?' )
				var els = document.querySelectorAll("svg .highlight");
				for (var i = 0; i < els.length; i++ ) {
					els[i].classList.remove("highlight");
				}
				var cursor = document.querySelector("#paper svg .abcjs-cursor");
				if (cursor) {
					cursor.setAttribute("x1", 0);
					cursor.setAttribute("x2", 0);
					cursor.setAttribute("y1", 0);
					cursor.setAttribute("y2", 0);
				}
			};
		}

		var cursorControl = new CursorControl();