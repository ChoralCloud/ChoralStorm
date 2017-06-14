package main

import (
    "fmt"
    "log"
    "net/http"
    "httprouter"
    "io/ioutil"
    "bytes"
    "time"
    "encoding/json"
    "math/rand"
)

type Payload struct {
    DeviceId        int                 `json:"device_id"`
    DeviceData      json.RawMessage     `json:"device_data"`
    DeviceTimestamp int64               `json:"device_timestamp"`
} 

func Index(w http.ResponseWriter, r *http.Request, _ httprouter.Params) {
    fmt.Fprintf(w, "Choral Device Endpoint\n")
}

func SendPayloadToChoralStorm(w http.ResponseWriter, r *http.Request, _ httprouter.Params){
    body, err := ioutil.ReadAll(r.Body)

    defer r.Body.Close()
    
    log.Printf("%s\n", body)

    req, err := http.NewRequest("POST", "http://localhost:3030/", bytes.NewBuffer(body))
    req.Header.Set("Content-Type", "application/json")

    client := &http.Client{}
    resp, err := client.Do(req)
    if err != nil {
        panic(err)
    }
    defer resp.Body.Close()

    fmt.Println("response Status:", resp.Status)
    fmt.Println("response Headers:", resp.Header)
    respBody, _ := ioutil.ReadAll(resp.Body)
    fmt.Println("response Body:", string(respBody))

    return
}

func handleRequests() {
    router := httprouter.New()
    router.GET("/", Index)
    router.POST("/", SendPayloadToChoralStorm)
    log.Fatal(http.ListenAndServe(":3000", router))
}

func main() {
    //go test()
    handleRequests()
}

func test() {
    time.Sleep(time.Second * 1)
    min := -50.0;
    max := 50.0;
    for {
        time.Sleep(time.Second * 1)
        // fmt.Sprintf("at %v, %s", e.When, e.What)
        t1 := fmt.Sprintf("{\"temperature\":\"%f\"}", (rand.Float64() * (max - min)) + min)
        t2 := fmt.Sprintf("{\"temperature\":\"%f\"}", (rand.Float64() * (max - min)) + min)
        t3 := fmt.Sprintf("{\"temperature\":\"%f\"}", (rand.Float64() * (max - min)) + min)

        d1 := Payload{1, []byte(t1), time.Now().UnixNano() / int64(time.Millisecond)}
        d2 := Payload{2, []byte(t2), time.Now().UnixNano() / int64(time.Millisecond)}
        d3 := Payload{3, []byte(t3), time.Now().UnixNano() / int64(time.Millisecond)}

        j1, err := json.Marshal(&d1)
        go postRequest(j1)

        j2, err := json.Marshal(&d2)
        go postRequest(j2)

        j3, err := json.Marshal(&d3)
        go postRequest(j3)

        if err != nil {
            panic(err)
        }
    }
}

func postRequest(j []byte) {
    req, err := http.NewRequest("POST", "http://localhost:3030/", bytes.NewBuffer(j))
    req.Header.Set("Content-Type", "application/json")

    client := &http.Client{}
    resp, err := client.Do(req)
    if err != nil {
        panic(err)
    }
    defer resp.Body.Close()
}