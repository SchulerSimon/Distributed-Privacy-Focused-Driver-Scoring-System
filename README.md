# Prototype for my Bachelor's Thesis

### Disclaimer:
This is a prototype under construction. You are (within the boundaries of the gnu general public license) free to do what ever you want with this code. 

### Contents:
- `/android_app` contains a prototype app written in java
- `/obd2_sim` contains an arduino based simulation tool for sending data via can-bus to an obd2 bluetooth-dongle
- `server_linux` contains a python3 implementation of the server-component that manages a localy deployed [burrow blockchain](https://github.com/hyperledger/burrow)

## Thesis proposal:
### Introduction
In the automotive insurance industry there are currently various approaches to
minimise the risk of bad driver behaviour. Data related to some characteristics
of the individual driver are gathered through multiple ways, including inter-
views, standard forms, etc. This data is then used to calculate the risk for
insuring a specific person. An example would be that the insurant gets better
conditions when he/she drives crash and incident free for a specific time period.
Another approach at gathering individual information is to monitor the driving
behaviour of a person while driving. Pricing the insurant on that basis min-
imises the cost for the insurance company overall. According to [7] this is called
”usage-based pricing”.

### State of the Art and Problems
Most current driver scoring systems are put in place on a small scale and orig-
inate from the insurance companies themselves or contracted third parties [4].
This leads to several problems: First, there is always an information asymmetry
between the insurant and the insurance. The consequence is that the insurance
could withhold information, that might either not be profitable for the com-
pany or even be beneficial for the driver, which might lead to unfair treatment
towards the insurant. Secondly the insurance is gathering all the data and has
no reason to share it with the insurant, his/her attorneys or other parties the
insurant wants to share his information with. This puts the driver in subordi-
nation to the insurance. Arising from these problems the mentioned systems
fail to provide a decentralised and global way of sharing trusted information
between parties with different interests, thus making it nearly impossible for
two insurance companies to fight over a case, based on the same and trusted
information.

### Proposed Solution
How would one design a system that accounts for the problems mentioned
above? To approach this question, let us consider a view different questions
first: What measures could be taken to ensure immutability of data? Could we
design a system that enables the driver to control access to his data? Is there a
technology that could be the basis of all that?
At first one would need to make sure that the capturing device is ”tamper-proof”
and that captured data cannot be altered. This could be achieved by working
with car manufacturers to develop a built-in system that uses information pro-
vided by the on board computer. Secondly the data has to be stored in a place
that is controlled by a third party with no specific interest in the data (neither
the insurance, nor the insurant). This data would then need to be associated
with the driver in some way.
State of the art research and development on blockchains raise a solution to
all the questions above. With the right implementation it is possible to have
information on driving behaviour tied to the driver him-/herself. This enables
the driver to share his information with an insurance company and other parties
of interest (e.g.: car rental service, authorities, etc.).
The information asymmetry could be overcome by designing a system that stores
most of the gathered data on a blockchain. This makes sure that neither the
insurant nor the insurance company can alter the stored information. At last
the problem of sharing information in a decentralised way and on a global scale
solves itself by the nature of blockchain technology. This could increase competi-
tion between insurance companies on a global scale and be profitable for drivers.
The proposed system allows us to imagine even more useful applications. For
example, companies could start to monitor the behaviour of professional drivers,
or with the use of smart contracts it is possible to tie the insurance contract not
to a car, but solely to a person, enabling the possibility for easier and cheaper
car sharing.

### Placement of the Thesis
Currently there is no system which accounts the issues of protecting the driver
and his/her data as well as immutability of data that might be relevant to the
insurance. Another advantage of the proposed system using blockchain technol-
ogy is better and easier interoperability among different parties of interest, e.g.:
different car manufacturers and insurance companies. Driver scoring is nothing
new and there is some research on the topic to be found e.g.: [3] or [2]. There
is however no solution that would match this specific scenario, so tailoring of
those solutions would be necessary.
Currently blockchain technology is at a state where most applications with a
fair amount of complexity are not viable for an enterprise solution. Thus the
task at hand is more a proof-of-concept.
The proposed thesis would include a software prototype with all described func-
tionality that would run on a smart phone and make use (where possible) of
the OBDII(On-board diagnostics two) port of the car. The proposed ”tamper-
proof” hardware device is explicitly not part of this thesis.
To evaluate the driver scoring part of the proposed system one would need
a OBDII-port Bluetooth dongle (10-15don Amazon), as well as simulation-
software/-hardware for the port. There is a software solution [1] that seems
nearly good enough for the purpose. All the Hardware emulators are quite ex-
pensive (at least 180d[6] and up to 2000d[8]), but would probably better suite
the purpose. The goal would be to use open data sets [5] that could be replayed
with the simulator in order to reliably test the system under specific conditions.
The development of this evaluation system would take place before the thesis
in a research project. To evaluate the viability of the whole system it is pro-
posed to do stress testing of the whole system. For example there could be test
cases with up to 1.000.000 cars. Thereby one could evaluate the capabilities in
performance of the system.

### Conclusion
Taking a step back we can clearly see the bigger picture now: The proposed
solution aims to solve problems in the insurance and car industry in a new
fashion, hopefully leading to innovation and progress in these fields. All in all
this thesis could take a step towards decentralisation and help working towards
fair treatment of customers.

### References
[1] chunky@icculus.org. OBDSim. url: http://icculus.org/obdgpslogger/
obdsim.html. (accessed: 15.10.2018).
[2] Isaac Sayo Daniel. System and method for determining an objective driver
score. Paper. F3M3 Cos Inc, 2008.
[3] Michael Greenlee Gregory Warren. Calculation of driver score based on
vehicle operation for forward looking insurance premiums. Paper. IVOX
NC, 2004.
[4] info@azuga.com. Azuga Driver Scoring. url: https://www.azuga.com/
products / insurance / azuga - advanced - driver - scoring/. (accessed:
16.10.2018).
[5] kaggle.com. url: https://www.kaggle.com/cephasax/obdii-ds3#exp3_
4drivers_1car_1route.csv. (accessed: 23.10.2018).
[6] mviljoen2. Arduino OBD2 Simulator. url: https://www.instructables.
com/id/Arduino-OBD2-Simulator/. (accessed: 17.10.2018).
[7] Kathleen Pender. Smartphone app from FICO, eDriving to score driving
behavior. url: https : / / www . sfchronicle . com / business / networth /
article/Smartphone-app-from-FICO-eDriving-to-score-10636484.
php. (accessed: 12.10.2018).
[8] OBD Solutions. ecusim-5100. url: https://www.obdsol.com/solutions/
development-tools/obd-simulators/ecusim-5100/. (accessed: 17.10.2018).



