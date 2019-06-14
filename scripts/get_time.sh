#!/bin/sh

for f in $(ls tests_exploit)
do
	awk '{print $3}' tests_exploit/$f | sed 's/]//' > date_tests/$f
done

