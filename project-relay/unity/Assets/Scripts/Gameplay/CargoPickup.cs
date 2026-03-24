using ProjectRelay.Gameplay;
using UnityEngine;

namespace ProjectRelay.Gameplay
{
    [RequireComponent(typeof(Collider))]
    public class CargoPickup : MonoBehaviour
    {
        [SerializeField] private float addWeightKg = 5f;
        [SerializeField] private bool destroyOnPickup = true;

        private void Reset()
        {
            var col = GetComponent<Collider>();
            col.isTrigger = true;
        }

        private void OnTriggerEnter(Collider other)
        {
            var weightSystem = other.GetComponent<LoadoutWeightSystem>();
            if (weightSystem == null) return;

            weightSystem.AddWeight(addWeightKg);
            Debug.Log($"[CargoPickup] +{addWeightKg}kg");

            if (destroyOnPickup)
                Destroy(gameObject);
        }
    }
}
