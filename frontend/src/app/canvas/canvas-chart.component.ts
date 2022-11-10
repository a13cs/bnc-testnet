import {AfterViewInit, Component, OnInit} from '@angular/core';
import {createChart, CrosshairMode, IChartApi, ISeriesApi, UTCTimestamp} from "lightweight-charts";
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
  selected: string = '1m';

  chart: any = {}
  private candlestickSeries: ISeriesApi<"Candlestick"> | undefined ;

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
    // ]);

    this.candlestickSeries = chart.addCandlestickSeries();


    chart.timeScale().fitContent();
    chart.priceScale().applyOptions({autoScale: true});

    // + ticker/price stream

    this.setData()
  }

  setData() {
    // close/klines

    let start = this.range.controls.start.value ? this.range.controls.start.value?.getTime() : new Date().getTime()  //new Date(Date.UTC(2022, 10, 10, 0, 0, 0, 0)).getTime() // 1000
    let end = this.range.controls.end.value ? this.range.controls.end.value?.getTime() : new Date().getTime()
    console.log("start: " + start)
    console.log("end: " + end)

    this.http.get<any[]>('klines/' +  start  + '/' +  end  + '/' + this.selected).subscribe(
      d => {
        // let lineData: any[] = []
        let data: any[] = []

        d.slice(0).forEach( point => {
//           console.log(+point[0])
          if(+point[0]) {
            data.push({
              open: point[/*"openPrice"*/1] | 0,
              high: point[/*"highPrice"*/2] | 0,
              low: point[/*"lowPrice"*/3] | 0,
              close: point[/*"closePrice"*/4] | 0,
              time: +point[/*"startTime"*/0] /1000 as UTCTimestamp
            })
            // let p : number = (Math.round(point[4] * 1000) / 1000)//.toFixed(2);
            // lineData.push({time: +point[0]/1000 as UTCTimestamp, value: point[4] | 0})
          }
        })
        this.candlestickSeries?.setData(data);

        this.getKLineEvent().subscribe(d => {
          let message = JSON.parse(d);
          // console.log(message)

          let bar = {
            time: +message['t']/1000 as UTCTimestamp | '',
            open:  message['o'],
            high:  message['h'],
            low:   message['l'],
            close: message['c']
          };

          // console.log('bar: ', d)
          this.candlestickSeries?.update(bar);
          // chart.timeScale().fitContent();
        });
      })

  }

  getKLineEvent(): Observable<any> {
    // let interval = '3m'

    return Observable.create((observer: { next: (arg0: string) => void; error: (arg0: string) => any; }) => {
      const eventSource = new EventSource(`sub/kLines/` + this.selected);

      eventSource.onmessage = (event) => {
        // console.log('eventSource.onmessage: ', event);
        // const json = JSON.parse(event.data);
        // console.log(json)
        observer.next(event.data);
      };
      eventSource.onerror = (error) => observer.error('eventSource.onerror: ' + error);
      return () => eventSource.close();
    });
  }

  onSelect(event? : any) {
    this.selected = event
    console.log(this.range.controls.start.value)
    console.log(event)


    this.setData()
  }

  picker() {
    console.log(this.range)
    this.setData()
  }
}
