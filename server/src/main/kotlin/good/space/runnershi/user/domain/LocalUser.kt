package good.space.runnershi.user.domain

import good.space.runnershi.model.domain.auth.Sex
import good.space.runnershi.model.domain.auth.UserType
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class LocalUser (
    @Column(nullable = false)
    var password: String,
    name: String,
    email: String,
    sex: Sex
) : User(
    name = name,
    email = email,
    userType = UserType.LOCAL,
    sex = sex
)
