---
title: "SLR210 project: report"
author:
- Aurélien Blicq
- Louis Penet de Monterno
geometry: margin=2cm
---

# High-level description

The system consists of a number of processes, communicating with each other to come to an agreement about a value, 0 or 1, using a version of the Paxos protocol.

This protocol works in two main steps:

1. Prepare phase:

- first, a process sends a ballot number to all the other processes, acting as a sort of priority number.

- When a process receives this ballot number, if it has not received a demand with a higher ballot number, it must promise the sender not to accept proposals for demands with smaller ballot numbers. Moreover, if it is about to decide on a value, the receiving process must send the almost decided value to the sender.
If the received ballot is smaller than the ballot the receiver is already committed to, the process sends an abort message so that the sender can restart a proposal with a new ballot number.

2. Accept phase:

- When the proposer receives promises from at least half of the processes, it first determines the value it must propose for acceptation. This value is the one associated to the highest ballot number among the proposal about to be decided. If no such value exists, the proposer proceeds with its own value.

- Upon receiving an accept demand, a process verifies once more that it has not committed to a process with a higher ballot number, or accepted the value with a higher ballot number. In that case, the process sends abort to the sender. Otherwise, the receiver sends back an acknowledgment to the sender.

- Finally, when the proposer receives acknowledgments from at least half the processes, the value is decided, and a decide message is sent to all.

This protocol has the advantage of still being able to produce results even though some processes (no more than half of them) a fault-prone.

\newpage

# Pseudo-code

# Proof of correctness

## Validity

__if a value is decided, it was proposed by at least one process :__

trivially true

## Aggreement

__no two process can decide different values :__

Let's assume that two process make different decision. Then we can deduce that :

1. These two precesses have received a __DECIDE__ message, containing two different values.
2. Let p and q be respectively the senders of these messages. Then p and q have received a majority of acknoledgments (i.e. __ACK__ messages) in response of attempts of imposing ballots.
Without loss of generality, let's assume that q has the highest ballot number.
3. If q tried to impose a ballot, it means that it has received __GATHER__ messages from a quorum. This quorum intersects with the quorum with received and acknoledged the __IMPOSE__ messages from p.
Let r be a process in the intersection.
4. There are two possibilities :
	- Either r received first the __READ__ request from q, then the __IMPOSE__ request from p. Then, when r received __READ__ request, it learned q's ballot number which is higher than p's one.
	Then, instead of acknoledging p's __IMPOSE__ request, r would have aborted.
	- Or r received first the __IMPOSE__ request from p, then the __READ__ request from q.
	In that case, while responding to q, r would have learnt it that p has already imposed a value. Then instead of imposing its own value, q would have imposed a more recent one.
	This case is also impossible.

## Obstruction-free termination

__If a process proposes, it eventually decides or aborts.__

When a process p proposes :

1. It sends __READ__ request to all.
2. Since the channels are reliable, every process eventually receive and respond.
3. p eventually receive either responses from a quorum, or aborts.
4. In the first case, it tries to impose a value.
5. It sends an __IMPOSE__ request to everybody, which eentually respond.
6. Eventually, p aborts, or receive acknoledgment from a quorum, then decides.

__If a correct process decides, no process abort indefinitely often.__

If a correct process decides, no other process will decide an other value later (cf previous section).
Then no process can try to impose a ballot with higher ballot number than the accepted one. If it would be the case, this new proposal would be decided.
Thus, the highest value __imposeballot__ kept in the system is the value previously decided. Then each time a process tries to read the system, the already decided ballot will be kept
as the highest knwn ballot, and will eventually be decided again.
No process can abort indefinitely often.

__If there is a time after which a single correct process p proposes a value sufficiently many times, p eventually decides.__

The algorith aborts each time a __READ__ or __IMPOSE__ request is received, but a higher ballot number is known.
In the case where all process stop proposing but one, the ballot number of this process will be increased each time a ballot is proposed. At some point, the ballot number of the proposals 
will be greater than any ballot number previously known by the system. Then the process can no longer abort. It will eventually decide.

# Performance analysis
