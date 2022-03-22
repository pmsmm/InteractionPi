# InteractionPi

The InteractionPi is the process that was designed, in the context of my master thesis, to run on Raspberry Pi's in order to communicate with the interactions that were developed for this work. This process has the main responsability of creating the bridge between the [Master Pi](https://github.com/pmsmm/MasterPi) and the interactions that are connected to the Raspberry Pi where this process is running.

The InteractionPi has several functions besides the one specified above which can be seen as the most general of its functions. In specific the InteractionPi is responsible for:

- Issuing commands to the Interactions and getting back responses from them;
- Receiving messages from Interactions and acknowledging them;
- Check if interactions are working by periodically pinging them;
- Gathering responses from interactions to send them to the Master Pi;
- Provide interfaces to easily manage interactions;
- Etc...

The way the InteractionPi is designed is to communicate with the Interactions via Serial. This communication happens through a wired connection to the Interactions via a USB cable. Speaking of interactions, below you can find links to all of the interactions that were developed to run on the InteractionPi:

- [Ultrasonic Distance Interaction](https://github.com/pmsmm/Ultrasonic-Distance-Interaction);
- [Coordinated RFID Cards Interaction](https://github.com/pmsmm/Coordinated-RFID-Cards-Interaction);
- [Telephone Exchange Interaction](https://github.com/pmsmm/Telephone-Exchange-Interaction);
- [Light Sensor Interaction](https://github.com/pmsmm/Light-Sensor-Interaction);
- [Simon Says Interaction](https://github.com/pmsmm/Simon-Says-Interaction);
- [Digital Safe Interaction](https://github.com/pmsmm/Digital-Safe-Interaction);
- [Morse Code Interaction](https://github.com/pmsmm/Morse-Code-Interaction);
- [RGB LED Interaction](https://github.com/pmsmm/RGB-LED-Interaction);
- [Ordered Numbers Interaction](https://github.com/pmsmm/Ordered-Numbers-Interaction);
- [Crypto Servo Interaction](https://github.com/pmsmm/Crypto-Servo-Interaction);
- [Keypad Interaction](https://github.com/pmsmm/Keypad-Interaction);
- [Binary Number Interaction](https://github.com/pmsmm/Binary-Number-Interaction);
