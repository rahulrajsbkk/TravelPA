package `in`.ouon.travelpa.model

class LocationModel {
    var title : String? = null
    var desc : String? = null
    var image : String? = null
    var price : Double = 0.00
    var id : String = ""
    var lat : Double = 0.0
    var lng : Double = 0.0

    constructor(){

    }

    constructor(title: String?, desc: String?, price : Double, image: String?, id: String, lat: Double, lng: Double) {
        this.title = title
        this.desc = desc
        this.price = price
        this.image = image
        this.id = id
        this.lat = lat
        this.lng = lng
    }


}
