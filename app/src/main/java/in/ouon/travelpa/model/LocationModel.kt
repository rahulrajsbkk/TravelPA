package `in`.ouon.travelpa.model

class LocationModel {
    var title: String? = null
    var desc: String? = null
    var image: String? = null
    var id: String? = null

    constructor() {

    }

    constructor(title: String?, desc: String?, image: String?, id: String?) {
        this.title = title
        this.desc = desc
        this.image = image
        this.id = id
    }


}
