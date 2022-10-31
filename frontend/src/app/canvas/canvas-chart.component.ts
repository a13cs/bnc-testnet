import {AfterViewInit, Component, OnInit} from '@angular/core';
import {createChart, CrosshairMode, UTCTimestamp} from "lightweight-charts";
import {FormControl, FormGroup} from "@angular/forms";
import {HttpClient} from "@angular/common/http";
import {Observable, tap} from "rxjs";

@Component({
  selector: 'canvas-chart',
  templateUrl: 'canvas-chart.component.html',
  styles: [`
    .chart-controls {
      display: inline;
      padding-right: 1em;
      position: static;
    }
  `]
})
export class CanvasChartComponent implements OnInit, AfterViewInit {

  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    let elementId = "chart-container";
    let elementById : HTMLElement  = document.getElementById(elementId) as HTMLElement ;

    const chart = createChart(elementById,{
      width: 900,
      height: 500,
      timeScale: {
        // barSpacing: 4,
        timeVisible: true,
        secondsVisible: true,
      },
      crosshair: {
        mode: CrosshairMode.Normal,
      }
    })

    chart.applyOptions({
      watermark: {
        color: 'rgba(11, 94, 29, 0.4)',
        visible: true,
        text: 'Positions',
        fontSize: 24,
        horzAlign: 'left',
        vertAlign: 'bottom',
      },
    });

    // const areaSeries = chart.addAreaSeries({
    //   // bottomColor: "red"
    // });
    // areaSeries.setData([
    //   { time: '2018-12-22', value: 32.51 },
    //   { time: '2018-12-23', value: 31.11 },
    //   { time: '2018-12-24', value: 27.02 },
    //   { time: '2018-12-25', value: 27.32 },
    //   { time: '2018-12-26', value: 25.17 },
    //   { time: '2018-12-27', value: 28.89 },
    //   { time: '2018-12-28', value: 25.46 },
    //   { time: '2018-12-29', value: 23.92 },
    //   { time: '2018-12-30', value: 22.68 },
    //   { time: '2018-12-31', value: 22.67 },
    // ]);

    const candlestickSeries = chart.addCandlestickSeries();
    // candlestickSeries.setData([
    //   { time: '2018-12-22', open: 75.16, high: 82.84, low: 36.16, close: 45.72 },
    //   { time: '2018-12-23', open: 45.12, high: 53.90, low: 45.12, close: 48.09 },
    //   { time: '2018-12-24', open: 60.71, high: 60.71, low: 53.39, close: 59.29 },
    //   { time: '2018-12-25', open: 68.26, high: 68.26, low: 59.04, close: 60.50 },
    //   { time: '2018-12-26', open: 67.71, high: 105.85, low: 66.67, close: 91.04 },
    //   { time: '2018-12-27', open: 91.04, high: 121.40, low: 82.70, close: 111.40 },
    //   { time: '2018-12-28', open: 111.51, high: 142.83, low: 103.34, close: 131.25 },
    //   { time: '2018-12-29', open: 131.33, high: 151.17, low: 77.68, close: 96.43 },
    //   { time: '2018-12-30', open: 106.33, high: 110.20, low: 90.39, close: 98.10 },
    // ]);

    chart.timeScale().fitContent();
    chart.priceScale().applyOptions({autoScale: true});

    // + ticker/price stream

    this.getKLineEvent().subscribe(d => {
      let message = JSON.parse(d);
      console.log(message)

      let bar = {
        time: +message['t']/1000 as UTCTimestamp | '',
        open:  message['o'],
        high:  message['h'],
        low:   message['l'],
        close: message['c']
      };

      console.log('bar: ', d)
      candlestickSeries.update(bar);

    });


  }

  getKLineEvent(): Observable<any> {
    let interval = '3m'

    return Observable.create((observer: { next: (arg0: string) => void; error: (arg0: string) => any; }) => {
      const eventSource = new EventSource(`sub/kLines/` + interval);

      eventSource.onmessage = (event) => {
        console.log('eventSource.onmessage: ', event);
        // const json = JSON.parse(event.data);
        // console.log(json)
        observer.next(event.data);
      };
      eventSource.onerror = (error) => observer.error('eventSource.onerror: ' + error);
      return () => eventSource.close();
    });
  }

  logRange() {
    console.log(this.range.controls.start.value)
  }
}
