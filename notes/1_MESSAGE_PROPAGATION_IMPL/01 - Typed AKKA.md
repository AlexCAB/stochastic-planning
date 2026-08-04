**Documentation**: 
* [Learning Akka Typed from Classic](https://doc.akka.io/libraries/akka-core/current/typed/from-classic.html)
* [Introduction to Actors](https://doc.akka.io/libraries/akka-core/current/typed/actors.html)
* [#Integrating Akka with Cats-Effect 3](https://alexn.org/blog/2023/04/17/integrating-akka-with-cats-effect-3/)

## Notes
---

**SBT commands:**
* `sbt compile`
* `sbt scalafmt`




## Sticky notes 

---

```
def traverse_[G[_], B](f: A => G[B])(implicit G: Applicative[G]): G[Unit] =  traverseVoid[G, B](f)
def  traverse[G[_], B](f: A => G[B])(implicit ev$1: Applicative[G]): G[F[B]] = typeClassInstance.traverse[G, A, B](self)(f)


"During processing message:\n"















```




