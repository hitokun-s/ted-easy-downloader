import groovy.json.JsonSlurper

def conf = new JsonSlurper().parseText(new File("config.json").text)
assert conf["download-dir"]
def dir = new File(conf["download-dir"])
assert dir.exists() && dir.directory

new File("downloadList").eachLine {url ->
    def title = url.split("/").last()

    println "start to download : ${title}"

    def file = new File(dir, title)
    if(file.exists()){
        println "already exists! : ${title}"
        return
    }
    file << new URL(url).openStream()
}