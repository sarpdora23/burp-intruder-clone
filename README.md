# Custom Burp Intruder Clone

A highly optimized, custom fuzzing engine extension for Burp Suite designed to overcome UI freezing issues with large datasets, providing an exact native feel via the Montoya API and Java Swing.

## Özellikler

- **Sanal Tablo (Anti-Freeze Virtual Table):** Yüz binlerce veya milyonlarca request barındıran devasa büyüklükteki wordlistler ile test yaparken Burp Suite'in donmasının veya RAM'i doldurmasının önüne geçer. Sonuçları bellek ve yavaşlama sorunu olmadan Lazy Loading ile ekrana yansıtır.
- **Side-by-Side (Bölünmüş Ekran) UX:** Burp Suite'in yeni nesil bölünmüş sekme arayüzü baz alınarak tasarlanmıştır. Sağ kısımda esnek Payload Configuration alanı bulunur.
- **Context Menu Entegrasyonu:** `HTTP History` veya `Repeater` üzerinden herhangi bir isteğe sağ tıklayıp anında "Send to Custom Intruder" gönderimi yapabilirsiniz.
- **Kesin (ISO-8859-1 Byte Exact) Enjeksiyon:** Payload işaretleyicisi (`§`) için geliştirilmiş byte bazlı şablonlama (templating). Karşılaşabileceğiniz encoding offset bozulmalarını engeller.
- **Hız ve Concurrency (Rate Limit):** Saldırı başlatmadan önce *Threads* ve *Throttle (ms)* ayarlarıyla sunucuyu yormadan, hız kontrollü ve paralel request gönderimi.
- **Durdurma Butonu:** Süreci veya atakları izlerken anında işlemi kesebileceğiniz "Stop Attack" seçeneği.

## Kurulum ve Yükleme

1. Projeyi bilgisayarınıza klonlayın.
2. Projeyi derleyerek `.jar` çıktısını alın (Gradle gereklidir):
   ```bash
   gradle build
   ```
3. Derleme sonucunda **`build/libs/`** klasörü içerisinde `custom-intruder-clone-1.0-SNAPSHOT.jar` adında bir dosya oluşacaktır.
4. **Burp Suite** programını açın.
5. `Extensions` > `Installed` sekmesine gelin ve `Add` butonuna tıklayın.
6. `Extension Type` ayarını **Java** olarak seçin.
7. İlgili konumu göstererek `.jar` dosyasını dahil edin. Eklenti saniyeler içerisinde "Custom Intruder" sekmesiyle menüye eklenecektir.

## Kullanım
- İstediğiniz bir http paketine sağ tıklayıp "Send to Custom Intruder" seçeneğiyle target sekmesini otomatik doldurabilirsiniz.
- İstek Editörü üzerinden istediğiniz stringleri seçerek "Add §" ile payload işaretlemesi yapabilirsiniz.
- Sağ bölümdeki `Payloads` panelinden "Load", "Paste" fonksiyonlarını kullanarak wordlist seçebilirsiniz.
- Hazır olduğunuzda yukarıdaki turuncu seçenekten "Start attack" butonuna basıp sonuçları analiz edebilirsiniz.

## Lisans
Bu proje geliştirme amaçlı açık kaynaklıdır. İzinsiz/illegal sistemlere karşı fuzzing işlemlerinde kullanılması sorumluluğu son kullanıcıya aittir.
