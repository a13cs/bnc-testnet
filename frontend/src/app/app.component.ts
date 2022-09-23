import {Component, OnInit} from '@angular/core';
import {createChart, CrosshairMode, ISeriesApi, UTCTimestamp} from 'lightweight-charts';
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  proxyConf = false


  title = 'app';
  indicatorVisible = false

  smaLineSecond: any
  smaLineFirst: any
  closeLine: any

  csvTimestamp: any
  data: any[] = []
  series: any = []

  acc: Acc = {accountType: "SPOT", balances: []}
  balances: Balance[] = [];
  myTrades: any [] = [];
  showTrades: boolean = false

  lastBar: any;
  barDuration: number = 1;
  prefix = this.proxyConf ? '/be' : ''

  constructor(private http: HttpClient) {
  }

  getJar() {
    this.http.get<Acc>(this.prefix + '/jar').subscribe( d => {
//       console.log(d)

    })

  }

//   showIndicator(show: boolean, emaLength: string /*short/long*/){
//     this.indicatorVisible = show
//     if(!show) {
//       (<ISeriesApi<"Line">>this.smaLineSecond).setData([])
//     } else {
//       this.http.get<any[]>(this.prefix + '/indicator/' + emaLength + '/0/'+this.csvTimestamp).subscribe(
//         d => {
//           console.log(d)
//
//           let indicatorData: any[] = []
//           for (let i = 1; i < this.data.length; i++) {
//             indicatorData.push({time: +this.data[i].time /*startTime*/ as UTCTimestamp, value: +d[i] | this.data[i].close})
//           }
//           this.smaLineSecond.setData(indicatorData)
//         }
//       );
//     }
//   }

  ngOnInit(): void {
//     this.getBarDuration()

    // create charts

    const chart = createChart(document.body, {
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

    this.series = chart.addCandlestickSeries();

//     // long
//     this.smaLineFirst = chart.addLineSeries({
//       color: 'rgb(10,22,125)',
//       lineWidth: 3,
//     });
//
//     this.smaLineSecond = chart.addLineSeries({
//       color: 'rgb(4,107,232)',
//       lineWidth: 2,
//     });

    // todo: add volume
    // chart.addHistogramSeries()

    // let volumeSeries = chart.addHistogramSeries({
    //   color: '#26a69a',
    //   priceFormat: {
    //     type: 'volume',
    //   },
    //   priceScaleId: '',
    //   scaleMargins: {
    //     top: 0.8,
    //     bottom: 0,
    //   },
    // });
    // {time, value, color}


//     const chartLine = createChart(document.body, {
//       width: 600,
//       height: 300,
//       layout: {
//         backgroundColor: '#ffffff',
//         textColor: 'rgba(33, 56, 77, 1)',
//       },
//       grid: {
//         vertLines: {
//           color: 'rgba(197, 203, 206, 0.7)',
//         },
//         horzLines: {
//           color: 'rgba(197, 203, 206, 0.7)',
//         },
//       },
//       timeScale: {
//         timeVisible: true,
//         secondsVisible: true,
//         // fixLeftEdge: true
//       },
//     });
//
//     chartLine.applyOptions({
//       watermark: {
//         color: 'rgba(2,28,6,0.4)',
//         visible: true,
//         text: 'Close Price Line Chart',
//         fontSize: 24,
//         horzAlign: 'left',
//         vertAlign: 'bottom',
//       },
//     });
//
//     // TODO: add trade quantity dots graph
//     this.closeLine = chartLine.addLineSeries({
//       color: 'rgb(17,10,151)',
//       lineWidth: 2,
//     });

    //  ===============================================================================

    // load data

    this.getData()

    this.http.get<Acc>(this.prefix + '/acc').subscribe( d => {
      this.acc = d
      console.log(d)
      this.balances = d.balances.filter(b => b.asset == 'BTC' || b.asset == 'USDT') || []
    })


/*
    // this.http.get<any[]>(prefix + '/acc').subscribe(
    this.http.get<any[]>('/acc').subscribe(
      d => {
        // let data : any = Object.keys(d)
        console.log("BTC SPOT Balance: " + JSON.stringify(d))
        this.balance = d;
      }
    )
*/


    // TODO: wss
//   setInterval(() => this.http.get<any>(this.prefix + '/lastTrade').subscribe(
//     price => {
//       console.log("Price: " + price)
//       let now = new Date().getTime() / 1000
//
//       // console.log(now)
//       // console.log(this.lastBar.time)
//
//       let endBarDiff = Math.round(now - this.lastBar?.time );
//       console.log("EndBarDiff: " + endBarDiff);
//
//       if(endBarDiff > this.barDuration) {
//         this.getData()
//         console.log(this.lastBar);
//
//         return;
//         // todo: update() with new bar
//       }
//
//       if (this.lastBar?.time) (<ISeriesApi<"Candlestick">>this.series).update({
//           time: this.lastBar.time,
//           open: this.lastBar.open,
//           high: +price > this.lastBar.high ? +price : this.lastBar.high,
//           low: +price < this.lastBar.low ? +price : this.lastBar.low,
//           close: +price
//         })
//     }), 1_000)
//
  }

//   getBarDuration() {
//     this.http.get<any>(this.prefix + "/barDuration").subscribe(
//       duration => this.barDuration = duration)
//   }

  getData() {
    let start = new Date(Date.UTC(2022, 8, 20, 0, 0, 0, 0)).getTime() // 1000
    let end = new Date(Date.UTC(2022, 9, 1, 0, 0, 0, 0)).getTime() // 1000
    console.log("start: " + start)
    console.log("end: " + end)

    let interval = '5m'
    this.http.get<any[]>(this.prefix + '/klines/' + start + '/' + end + '/' + interval).subscribe(
      d => {
        let lineData: any[] = []
        // console.log("bars: " + JSON.stringify(d))

        this.csvTimestamp = d.slice(0,1)[0][0];
        console.log("csvTimestamp: " + this.csvTimestamp)

//         d.slice(1).forEach( point => {
//           if(+point[1]) {
//             this.data.push({
//               open: point[/*"openPrice"*/3] | 0,
//               high: point[/*"highPrice"*/5] | 0,
//               low: point[/*"lowPrice"*/6] | 0,
//               close: point[/*"closePrice"*/4] | 0,
//               time: +point[/*"endTime"*/1]
//             })
//             // let p : number = (Math.round(point[4] * 1000) / 1000)//.toFixed(2);
//             lineData.push({time: +point[1] as UTCTimestamp, value: point[4] | 0})
//           }
//         })
        d.slice(0).forEach( point => {
//           console.log(+point[0])
          if(+point[0]) {
              this.data.push({
                open: point[/*"openPrice"*/1] | 0,
                high: point[/*"highPrice"*/2] | 0,
                low: point[/*"lowPrice"*/3] | 0,
                close: point[/*"closePrice"*/4] | 0,
                time: +point[/*"startTime"*/0] /1000 as UTCTimestamp
              })
              // let p : number = (Math.round(point[4] * 1000) / 1000)//.toFixed(2);
              lineData.push({time: +point[0]/1000 as UTCTimestamp, value: point[4] | 0})
            }
        })
        this.lastBar = this.data[this.data.length-1]
        // console.log("lastBar: " + JSON.stringify(this.lastBar))

        this.series.setData(this.data);
//         this.closeLine.setData(lineData)

        this.http.get<any[]>(this.prefix + '/myTrades').subscribe( d => {
          this.myTrades = d

          let signals: any[] = d.map(s => {
//             console.log(s)
//             console.log(s['time'])
            return {
              time:  +s['time'] /1000 as UTCTimestamp,
              color: s['isBuyer'] === true ? 'rgb(7,130,19)' : 'rgb(113,10,11)',
              position: 'aboveBar',
              shape: s['isBuyer'] === true ? "arrowUp" : "arrowDown",
              text: (s['quoteQty']) || ''
            }
          })

          this.series.setMarkers(signals)
        })
      })

//       chart.timeScale().setVisibleRange({
//           from: (new Date(Date.UTC(2018, 0, 1, 0, 0, 0, 0))).getTime() / 1000,
//           to: (new Date(Date.UTC(2018, 1, 1, 0, 0, 0, 0))).getTime() / 1000,
//       });



  }




}

  export interface Acc {
    accountType: string,
    balances: Balance[]
  }

  export interface Balance {
    asset: string,
    free: string,
  //   locked: string
  }
