import groovy.json.JsonSlurper

@Grab(group='org.seleniumhq.selenium', module='selenium-firefox-driver', version='2.46.0')
@Grab(group='org.seleniumhq.selenium', module='selenium-support', version='2.46.0')

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait

def conf = new JsonSlurper().parseText(new File("config.json").text)
assert conf.lang
assert conf["selector-lang"]
assert conf.page

// How to resolve classes by @grab:
// if some class cannot be resolved on IDEA, please push alt + enter, and select 'grab',
// so the artifact will be added automatically.

// You can use any driver
def driver = new FirefoxDriver();
driver.get("https://www.ted.com/talks?language=${conf.lang}&page=${conf.page}")

def elms = driver.findElementsByTagName('a')
def talkUrlList = elms.findAll{
    it.getAttribute('href').startsWith('https://www.ted.com/talks/')
}.collect{it.getAttribute('href')}.unique()

println "collected movie counts : ${talkUrlList}"

def downloadListFile = new File('downloadList')
def ls = System.lineSeparator()

def exec = { url ->
    driver.get(url)

    // You need to wait until download button will be shown
    WebDriverWait wait = new WebDriverWait(driver, 300);
    WebElement downloadBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className('download-button')));
    downloadBtn.click()

    // select subtitle language
    try{
        new Select(driver.findElementByClassName('talk-download__language')).selectByVisibleText(conf["selector-lang"])
        downloadListFile <<  driver.findElementByClassName('talk-download__button--video').getAttribute('href') + ls
    }catch(Exception e){
        // Especially for the newest movie, there is no selection on subtitle selector
        println e
    }
}

talkUrlList.each(exec)

driver.close()