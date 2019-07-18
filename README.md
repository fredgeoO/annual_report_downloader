# annual_report_downloader
annual_report_downloader (HK Stock Market)

投資人需要看很多資料，尤其是年報，近來寫了個東西爬取所有香港股票年報。也分享給有需要的人。

年報來源是披露易，可以用來下載披露易上面的所有年報。

由於開發時間不多所以直接用Java，運行這個軟體需要裝Java Runtime Environment，Chrome和ChromeDriver， 
原理是用selenium模擬瀏覽器方式爬取，不用處理header，加密連接，和反爬蟲的問題，比較少麻煩，代價是Java和模擬器方式爬取很慢，
該下載器未在windows10以外的操作系統做過測試。
