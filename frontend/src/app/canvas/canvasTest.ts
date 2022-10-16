import { Component, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import {createChart, CrosshairMode, ISeriesApi, UTCTimestamp} from 'lightweight-charts';
import {HttpClient, HttpHeaders} from "@angular/common/http";


@Component({
   selector: 'my-component',
   template: `<canvas #myCanvas></canvas>`
})
export class CanvasTest implements AfterViewInit {
  @ViewChild('myCanvas', {static: false}) myCanvas: ElementRef = {} as ElementRef;

  public context: CanvasRenderingContext2D = {} as CanvasRenderingContext2D;


  constructor(private http: HttpClient) {
  }


  ngAfterViewInit(): void {
    this.context = this.myCanvas.nativeElement.getContext('2d');
    console.log(this.context)
    console.log(this.myCanvas)

    const chart = createChart(  this.context.canvas , {
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
    });

    this.http.get<any[]>('be' + '/klines/' + /* start */ 0 + '/' + /* end */ 0 + '/' + '1m').subscribe(
      d => {
        let data: any[] = []

        let csvTimestamp = d.slice(0,1)[0][0];
        console.log("csvTimestamp: " + csvTimestamp)

        console.log(d[0])
        d.slice(0).forEach( point => {
          if(+point[0]) {
              data.push({
                open: point[/*"openPrice"*/1] | 0,
                high: point[/*"highPrice"*/2] | 0,
                low: point[/*"lowPrice"*/3] | 0,
                close: point[/*"closePrice"*/4] | 0,
                time: +point[/*"startTime"*/0] /1000 as UTCTimestamp
              })
            }
        })
      chart.addCandlestickSeries().setData(data)
      console.log(chart)
    })

  }
}
